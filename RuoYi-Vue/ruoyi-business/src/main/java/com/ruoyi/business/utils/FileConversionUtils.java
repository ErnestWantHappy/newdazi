package com.ruoyi.business.utils;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.filter.NoopFilter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 文件转换工具类
 * 使用 JODConverter + LibreOffice 服务模式进行文档转换。
 * <p>
 * 自愈策略（方案 B）：转换路径上的即时重建 + 分钟级健康巡检熔断 + 重建冷却，
 * 避免半死进程干等 task-execution-timeout，同时降低误杀正常池的概率。
 */
@Component
public class FileConversionUtils {

    private static final Logger log = LoggerFactory.getLogger(FileConversionUtils.class);

    private static String libreOfficeHome = "C:\\Program Files\\LibreOffice";
    private static int officeInstanceCount = 5;
    private static int processWarnThreshold = 10;
    private static int maxTasksPerProcess = 30;
    private static long taskExecutionTimeout = 300_000L;
    private static long taskQueueTimeout = 120_000L;
    private static long processTimeout = 120_000L;
    private static int maxRetry = 1;
    /** 演示文稿/表格使用隔离进程，限制并发以免大量 soffice 抢占内存。 */
    private static final int MAX_ISOLATED_CONVERSIONS = 2;
    private static final Semaphore ISOLATED_CONVERSION_SLOTS =
            new Semaphore(MAX_ISOLATED_CONVERSIONS, true);
    /** 连续服务级失败达到该阈值才触发主动自愈 */
    private static int failureThreshold = 3;
    /** 两次整池重建最小间隔，防止抖动反复杀池 */
    private static long rebuildCooldownMs = 120_000L;
    /**
     * 在途转换超过该时间仍无完成，视为半死挂起信号。
     * 应明显小于 task-execution-timeout，以便 1～2 分钟内自愈。
     */
    private static long softHangTimeoutMs = 120_000L;
    /** 是否保留 jodconverter 默认的 RefreshFilter（完整重排），默认关闭，见 init 注释 */
    private static boolean refreshFilterEnabled = false;
    /** 同一源文件把共享池打崩多少次后改走隔离进程 */
    private static int poisonIsolateThreshold = 1;

    private static final ReentrantReadWriteLock OFFICE_MANAGER_LOCK = new ReentrantReadWriteLock(true);
    private static final Lock OFFICE_READ_LOCK = OFFICE_MANAGER_LOCK.readLock();
    private static final Lock OFFICE_WRITE_LOCK = OFFICE_MANAGER_LOCK.writeLock();

    private static final AtomicInteger serviceFailureCount = new AtomicInteger(0);
    private static final AtomicInteger consecutiveServiceFailures = new AtomicInteger(0);
    private static final AtomicInteger inFlightConversions = new AtomicInteger(0);
    private static final AtomicLong oldestInFlightStartMs = new AtomicLong(0L);

    /**
     * 在途转换“世代”号。每次整池重建 +1。
     * 卡死的旧 convert 线程在重建后才返回并调用 endInFlight，
     * 若只按计数递减，它会把新一轮的在途数减掉，导致挂起判定失真。
     * 线程记下自己开始时的世代，只有世代相同才允许递减。
     */
    private static final AtomicLong inFlightEpoch = new AtomicLong(0L);
    /** 记录每个转换线程的开始时间与世代，重建时用于定位并中断卡死线程 */
    private static final ConcurrentHashMap<Thread, InFlightSlot> inFlightSlots = new ConcurrentHashMap<>();

    /**
     * 毒文件隔离表：源文件绝对路径 -> 已把共享池打崩的次数。
     * LibreOffice Writer 会在部分 docx 上 SIGABRT 自我终止（Signal 6），
     * 这类文件继续走共享池只会反复拖垮整池，命中后改走一次性隔离进程。
     * 不删除原始文件，仅改变转换通道。
     */
    private static final ConcurrentHashMap<String, AtomicInteger> poisonFileHits = new ConcurrentHashMap<>();

    /**
     * 任何通道都转换不出来的源文件（绝对路径 -> 原因）。
     * 隔离通道也失败后登记，用于停止无意义重试；原始文件始终保留。
     */
    private static final ConcurrentHashMap<String, String> unconvertibleFiles = new ConcurrentHashMap<>();

    /** 重建后尚未出现一次成功转换；此期间不得判定为健康，避免"假恢复" */
    private static volatile boolean awaitingPostRebuildSuccess = false;
    /**
     * 重建后已发起的转换次数。
     * 用于区分"重建后真的转不动"和"重建后根本没人提交、只是空闲"：
     * 没有这个门，深夜无人提交时也会被判定为不健康并反复重启。
     */
    private static final AtomicInteger conversionAttemptsSinceRebuild = new AtomicInteger(0);
    /** 连续重建且期间无成功转换的次数，用于指数退避，打断每 3～7 分钟重启一次的循环 */
    private static volatile int rebuildsWithoutSuccess = 0;

    private static volatile long lastSuccessAtMs = 0L;
    private static volatile long lastFailureAtMs = 0L;
    private static volatile long lastRebuildAtMs = 0L;
    private static volatile String lastRebuildReason = "";

    private static OfficeManager officeManager;
    private static DocumentConverter documentConverter;
    private static volatile boolean serviceAvailable = false;

    @Value("${ruoyi.libre-office.home:C:\\Program Files\\LibreOffice}")
    private String configuredOfficeHome;

    @Value("${ruoyi.libre-office.instance-count:5}")
    private int configuredInstanceCount;

    @Value("${ruoyi.libre-office.process-warn-threshold:10}")
    private int configuredProcessWarnThreshold;

    @Value("${ruoyi.libre-office.max-tasks-per-process:30}")
    private int configuredMaxTasksPerProcess;

    @Value("${ruoyi.libre-office.task-execution-timeout:300000}")
    private long configuredTaskExecutionTimeout;

    @Value("${ruoyi.libre-office.task-queue-timeout:120000}")
    private long configuredTaskQueueTimeout;

    @Value("${ruoyi.libre-office.process-timeout:120000}")
    private long configuredProcessTimeout;

    @Value("${ruoyi.libre-office.max-retry:1}")
    private int configuredMaxRetry;

    @Value("${ruoyi.libre-office.failure-threshold:3}")
    private int configuredFailureThreshold;

    @Value("${ruoyi.libre-office.rebuild-cooldown-ms:120000}")
    private long configuredRebuildCooldownMs;

    @Value("${ruoyi.libre-office.soft-hang-timeout-ms:120000}")
    private long configuredSoftHangTimeoutMs;

    /**
     * 是否在共享池转换链末尾保留 jodconverter 默认的 RefreshFilter（会对文档做一次完整重排）。
     * 默认 true，即保持既有行为：实测本批异常 docx 在隔离 CLI（无 RefreshFilter）下同样
     * SIGABRT 或挂起，说明崩溃源于文档本身而非该过滤器，因此不做未经证实的行为变更。
     * 保留开关，便于后续按需跳过这次强制重排。
     */
    @Value("${ruoyi.libre-office.refresh-filter-enabled:true}")
    private boolean configuredRefreshFilterEnabled;

    /**
     * 同一源文件把共享池打崩多少次后改走一次性隔离进程。
     * 默认 1：崩一次即隔离，避免同一毒文件反复拖垮整池。
     */
    @Value("${ruoyi.libre-office.poison-isolate-threshold:1}")
    private int configuredPoisonIsolateThreshold;

    @PostConstruct
    public void init() {
        libreOfficeHome = configuredOfficeHome;
        officeInstanceCount = Math.max(configuredInstanceCount, 1);
        // 隔离转换每份会短暂增加 soffice.exe + soffice.bin，健康检查不得把它误判为孤儿进程。
        processWarnThreshold = Math.max(configuredProcessWarnThreshold,
                officeInstanceCount * 2 + MAX_ISOLATED_CONVERSIONS * 2);
        maxTasksPerProcess = Math.max(configuredMaxTasksPerProcess, 1);
        taskExecutionTimeout = Math.max(configuredTaskExecutionTimeout, 30_000L);
        taskQueueTimeout = Math.max(configuredTaskQueueTimeout, 30_000L);
        processTimeout = Math.max(configuredProcessTimeout, 30_000L);
        maxRetry = Math.max(configuredMaxRetry, 0);
        failureThreshold = Math.max(configuredFailureThreshold, 1);
        rebuildCooldownMs = Math.max(configuredRebuildCooldownMs, 30_000L);
        refreshFilterEnabled = configuredRefreshFilterEnabled;
        poisonIsolateThreshold = Math.max(configuredPoisonIsolateThreshold, 1);
        // 软挂起超时不超过硬超时，且至少 30 秒，避免高峰排队误杀
        softHangTimeoutMs = Math.min(
                Math.max(configuredSoftHangTimeoutMs, 30_000L),
                Math.max(taskExecutionTimeout - 30_000L, 30_000L));
        startOfficeManager(true);
    }

    @PreDestroy
    public void destroy() {
        stopOfficeManager(true);
    }

    private static void startOfficeManager(boolean cleanOrphansBeforeStart) {
        OFFICE_WRITE_LOCK.lock();
        try {
            startOfficeManagerInternal(cleanOrphansBeforeStart);
        } finally {
            OFFICE_WRITE_LOCK.unlock();
        }
    }

    private static void stopOfficeManager(boolean killAfterStopFailure) {
        OFFICE_WRITE_LOCK.lock();
        try {
            stopOfficeManagerInternal(killAfterStopFailure);
        } finally {
            OFFICE_WRITE_LOCK.unlock();
        }
    }

    private static boolean startOfficeManagerInternal(boolean cleanOrphansBeforeStart) {
        if (isManagerRunningInternal()) {
            serviceAvailable = true;
            log.info("【LibreOffice服务】服务已在运行中");
            return true;
        }

        try {
            if (cleanOrphansBeforeStart) {
                killOrphanedOfficeProcesses();
            }

            log.info("【LibreOffice服务】正在启动服务模式（{}个实例）...", officeInstanceCount);
            int[] ports = buildPortNumbers();

            LocalOfficeManager newOfficeManager = LocalOfficeManager.builder()
                    .officeHome(libreOfficeHome)
                    .portNumbers(ports)
                    .maxTasksPerProcess(maxTasksPerProcess)
                    .taskExecutionTimeout(taskExecutionTimeout)
                    .taskQueueTimeout(taskQueueTimeout)
                    .processTimeout(processTimeout)
                    .build();

            newOfficeManager.start();
            officeManager = newOfficeManager;
            LocalConverter.Builder converterBuilder = LocalConverter.builder()
                    .officeManager(newOfficeManager);
            if (!refreshFilterEnabled) {
                // 不使用 jodconverter 默认链（末尾是 RefreshFilter，会对文档做完整重排）。
                // NoopFilter.CHAIN 什么都不做：不修改文档，直接交给 store 导出 PDF。
                // 实测 RefreshFilter 触发的重排会让 Writer 在部分 docx 上 SIGABRT。
                converterBuilder.filterChain(NoopFilter.CHAIN);
            }
            documentConverter = converterBuilder.build();
            serviceAvailable = true;
            serviceFailureCount.set(0);
            consecutiveServiceFailures.set(0);
            conversionAttemptsSinceRebuild.set(0);
            clearInFlightTracking();
            log.info("【LibreOffice服务】服务启动成功，支持{}个并发转换实例（refreshFilter={}）",
                    officeInstanceCount, refreshFilterEnabled);
            return true;
        } catch (Exception e) {
            officeManager = null;
            documentConverter = null;
            serviceAvailable = false;
            log.error("【LibreOffice服务】启动失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private static void stopOfficeManagerInternal(boolean killProcesses) {
        OfficeManager currentOfficeManager = officeManager;
        officeManager = null;
        documentConverter = null;
        serviceAvailable = false;

        // 先杀进程再 stop：避免 stop() 等待 task-execution-timeout（默认 5 分钟）把自愈堵死
        if (killProcesses) {
            killOrphanedOfficeProcesses();
        }

        if (currentOfficeManager == null) {
            return;
        }

        try {
            currentOfficeManager.stop();
            log.info("【LibreOffice服务】服务已停止");
        } catch (Exception e) {
            // 进程已杀时 stop 常抛异常，可忽略
            log.warn("【LibreOffice服务】服务停止异常（进程可能已清理）: {}", e.getMessage());
        }
    }

    private static boolean rebuildOfficeManager(String reason) {
        long now = System.currentTimeMillis();
        if (isInRebuildCooldown(now)) {
            log.warn("【LibreOffice服务】跳过重建（冷却中），原因={}", reason);
            return false;
        }
        // 半死挂起时不可无超时 lock，否则自愈链路一起卡死
        if (!acquireWriteLockForRecovery("重建:" + reason)) {
            return false;
        }
        try {
            // 双检：拿锁期间其他线程可能已重建成功
            if (isInRebuildCooldown(System.currentTimeMillis()) && isManagerRunningInternal() && serviceAvailable) {
                log.info("【LibreOffice服务】他线程已重建完成，跳过，原因={}", reason);
                return true;
            }
            log.warn("【LibreOffice服务】开始重建，原因={}", reason);
            // 先中断卡死的转换线程，再杀进程停池。
            // 否则卡在 convert 里的线程会变成僵尸继续占用 conversionExecutor
            //（130 上池仅 2 个线程，两个卡死即全池瘫痪），并在重建后错误回收新一轮的在途计数。
            int interrupted = interruptStuckConversions(0L, softHangTimeoutMs);
            if (interrupted > 0) {
                log.warn("【LibreOffice服务】重建前中断卡死转换线程 {} 个", interrupted);
            }
            stopOfficeManagerInternal(true);
            waitForOfficeShutdown();
            boolean started = startOfficeManagerInternal(false);
            markRebuild(reason, started);
            if (!started) {
                log.error("【LibreOffice服务】重建失败，原因={}", reason);
            }
            return started;
        } finally {
            OFFICE_WRITE_LOCK.unlock();
        }
    }

    private static int[] buildPortNumbers() {
        int[] ports = new int[officeInstanceCount];
        for (int i = 0; i < officeInstanceCount; i++) {
            ports[i] = 2002 + i;
        }
        return ports;
    }

    private static void waitForOfficeShutdown() {
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 清理残留的 LibreOffice / soffice 进程（Windows）。
     * 仅在首次启动前、优雅关闭失败或受控自愈场景使用。
     */
    private static void killOrphanedOfficeProcesses() {
        try {
            log.info("【LibreOffice服务】清理残留 soffice 进程...");
            killProcess("soffice.bin");
            killProcess("soffice.exe");
            Thread.sleep(2000L);
            log.info("【LibreOffice服务】残留进程清理完成");
        } catch (Exception e) {
            log.warn("【LibreOffice服务】清理残留进程时出现异常（可忽略）: {}", e.getMessage());
        }
    }

    private static void killProcess(String processName) throws Exception {
        if (!isWindows()) {
            // Linux 仅清理当前服务账号、当前端口池的 JODConverter，不能误杀其他程序的 Office。
            Process listing = new ProcessBuilder("ps", "-u", System.getProperty("user.name"),
                    "-o", "pid=,comm=,args=").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(listing.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!isManagedLinuxOfficeProcess(line, processName, buildPortNumbers())) continue;
                    String pid = line.trim().split("\\s+", 3)[0];
                    new ProcessBuilder("kill", "-KILL", pid).start().waitFor();
                }
            }
            listing.waitFor();
            return;
        }
        ProcessBuilder processBuilder = new ProcessBuilder("taskkill", "/F", "/IM", processName);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        process.waitFor();
    }

    /**
     * 使用 LibreOffice 服务模式将 DOCX 文件转换为 PDF。
     *
     * @param docxFilePath DOCX 源文件的绝对路径
     * @param outputDir PDF 输出目录的绝对路径
     * @return 转换后的 PDF 文件绝对路径，失败返回 null
     */
    public static String convertDocxToPdfWithLibreOffice(String docxFilePath, String outputDir) {
        return convertOfficeToPdfWithLibreOffice(docxFilePath, outputDir);
    }

    /**
     * 使用 LibreOffice 将 Word、PowerPoint 或 Excel 文件转换为 PDF。
     */
    public static String convertOfficeToPdfWithLibreOffice(String officeFilePath, String outputDir) {
        File officeFile = new File(officeFilePath);
        if (!officeFile.exists()) {
            log.error("源文件不存在: {}", officeFilePath);
            return null;
        }

        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            log.error("输出目录创建失败: {}", outputDir);
            return null;
        }

        String officeFileName = officeFile.getName();
        String pdfFileName = officeFileName.replaceFirst("(?i)\\.[^.]+$", ".pdf");
        if (pdfFileName.equals(officeFileName)) {
            pdfFileName = officeFileName + ".pdf";
        }
        String pdfFilePath = outputDir + File.separator + pdfFileName;
        File pdfFile = new File(pdfFilePath);

        if (requiresIsolatedCli(officeFilePath)) {
            // 演示文稿/表格：常驻管道路径会永久卡住，直接走一次性隔离进程
            return convertWithIsolatedOfficeProcess(officeFile, outputDirectory, pdfFile);
        }

        for (int attempt = 0; attempt <= maxRetry; attempt++) {
            // 毒文件检查必须放在循环内：第 1 次尝试把整池打崩后，本次调用就会把该文件
            // 登记为毒文件；重试必须立即改走隔离进程，否则同一次调用会把整池再打崩一次。
            if (isPoisonFileIsolated(officeFilePath)) {
                log.warn("【LibreOffice服务】源文件已命中隔离阈值，改走一次性隔离进程: {}", officeFilePath);
                String isolatedResult = convertWithIsolatedOfficeProcess(officeFile, outputDirectory, pdfFile);
                if (isolatedResult == null) {
                    // 隔离通道也救不回来：该文件本身不可转换，登记后停止无意义重试。
                    // 只登记不删文件——学生原始作品必须保留。
                    markUnconvertible(officeFilePath,
                            "文件内容导致 LibreOffice 无法转换（隔离转换同样失败）");
                }
                return isolatedResult;
            }

            if (!ensureOfficeManagerReady()) {
                log.error("【LibreOffice服务】服务不可用，无法进行转换: {}", officeFilePath);
                markServiceFailure("服务不可用");
                return null;
            }

            long startTime = System.currentTimeMillis();
            beginInFlight(startTime, officeFilePath);
            try {
                if (!executeConversionWithSharedLock(officeFile, pdfFile)) {
                    markServiceFailure("服务未就绪");
                    // 仅服务不可用时重建；避免单次抖动整池重启
                    if (attempt < maxRetry && tryRebuildIfNeeded("服务未就绪")) {
                        continue;
                    }
                    log.error("【服务模式转换】服务未就绪，转换失败: {}", officeFilePath);
                    return null;
                }

                long duration = System.currentTimeMillis() - startTime;
                if (pdfFile.exists()) {
                    log.info("【服务模式转换】成功: {} -> {} (耗时: {}ms)", officeFilePath, pdfFilePath, duration);
                    markServiceSuccess();
                    return pdfFilePath;
                }

                log.error("【服务模式转换】完成但 PDF 文件未生成: {}", pdfFilePath);
                markServiceFailure("PDF未生成");
                return null;
            } catch (OfficeException e) {
                // 进程数掉到实例数以下 = soffice 崩溃了，这是唯一能把“文档级失败”
                // 升级为服务级失败的证据。不要用异常文本判断：实测崩溃时上层拿到的
                // 是 “Local conversion failed”，不含任何连接/socket 关键字。
                boolean processCrashed = countOfficeProcesses() < officeInstanceCount;

                if (!isServiceLevelFailure(e) && !processCrashed) {
                    // 纯文档级失败：只记录，不计入服务失败计数。
                    // 旧实现这里调用 markServiceFailure，导致一份坏文档就能把
                    // consecutiveServiceFailures 推到阈值，触发整池重启——
                    // 这正是 130 上「9× 连续服务失败达到阈值(4>=3)」的来源。
                    log.error("【服务模式转换】文档转换失败，不影响服务健康: {} 文件={}", e.getMessage(), officeFilePath);
                    markDocumentFailureOnly();
                    return null;
                }

                if (!isServiceLevelFailure(e)) {
                    // 文档级异常 + 进程真的没了：判定为该文件把池打崩，登记毒文件
                    int hits = registerPoisonFileHit(officeFilePath);
                    log.warn("【服务模式转换】转换导致 soffice 进程减少（命中次数={}）: {}",
                            hits, officeFilePath);
                }

                int failureCount = serviceFailureCount.incrementAndGet();
                String rebuildReason = "服务级异常#" + failureCount + ": " + truncateMessage(e.getMessage());
                log.warn("【服务模式转换】检测到服务级异常: {}", rebuildReason);
                markServiceFailure(rebuildReason);

                // 取消/超时在高峰很常见；达到阈值且冷却结束后才整池重建，避免雪崩
                if (attempt < maxRetry) {
                    if (tryRebuildIfNeeded(rebuildReason) || isServiceAvailable()) {
                        continue;
                    }
                }

                log.error("【服务模式转换】服务级异常重试失败: {}", officeFilePath);
                return null;
            } finally {
                endInFlight();
            }
        }

        log.error("【服务模式转换】所有重试均失败: {}", officeFilePath);
        return null;
    }

    /**
     * 服务器上的 JODConverter 常驻管道对部分 PPT/Excel 会永久卡住；
     * 这些格式使用独立用户目录的一次性进程，既兼容 WPS/PowerPoint 文件，也不污染常驻 Word 转换池。
     */
    private static String convertWithIsolatedOfficeProcess(File officeFile,
                                                            File outputDirectory,
                                                            File pdfFile) {
        boolean acquired = false;
        Process process = null;
        // LibreOffice 在 Windows 下会静默忽略过深的用户目录，并以 0 退出但不产出 PDF；
        // 放到系统临时目录既缩短路径，又继续保证每次转换互相隔离。
        File profileRoot = new File(System.getProperty("java.io.tmpdir"), "newdazi-lo-profiles");
        File profileDir = new File(profileRoot, UUID.randomUUID().toString());
        File processLog = new File(outputDirectory,
                ".lo-convert-" + UUID.randomUUID().toString() + ".log");
        try {
            acquired = ISOLATED_CONVERSION_SLOTS.tryAcquire(taskQueueTimeout, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("【隔离模式转换】等待槽位超时: {}", officeFile.getAbsolutePath());
                return null;
            }
            if (!profileDir.mkdirs() && !profileDir.isDirectory()) {
                log.error("【隔离模式转换】用户目录创建失败: {}", profileDir.getAbsolutePath());
                return null;
            }
            if (pdfFile.exists() && !pdfFile.delete()) {
                log.error("【隔离模式转换】无法清理旧 PDF: {}", pdfFile.getAbsolutePath());
                return null;
            }
            // 可执行文件按平台依次尝试：Windows 控制台版、Windows 图形版、Linux 无后缀版。
            File executable = new File(libreOfficeHome,
                    "program" + File.separator + "soffice.com");
            if (!executable.isFile()) executable = new File(libreOfficeHome,
                    "program" + File.separator + "soffice.exe");
            if (!executable.isFile()) executable = new File(libreOfficeHome,
                    "program" + File.separator + "soffice");
            if (!executable.isFile()) {
                log.error("【隔离模式转换】找不到 LibreOffice 可执行文件: {}", libreOfficeHome);
                return null;
            }
            ProcessBuilder builder = new ProcessBuilder(
                    executable.getAbsolutePath(),
                    "-env:UserInstallation=" + libreOfficeUserProfileUri(profileDir),
                    "--headless", "--convert-to", "pdf",
                    "--outdir", outputDirectory.getAbsolutePath(),
                    officeFile.getAbsolutePath());
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.to(processLog));
            long startedAt = System.currentTimeMillis();
            process = builder.start();
            long timeout = Math.min(taskExecutionTimeout, 120_000L);
            if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                log.warn("【隔离模式转换】超时并终止: {}", officeFile.getAbsolutePath());
                return null;
            }
            long duration = System.currentTimeMillis() - startedAt;
            if (process.exitValue() == 0 && pdfFile.isFile()) {
                log.info("【隔离模式转换】成功: {} -> {} (耗时: {}ms)",
                        officeFile.getAbsolutePath(), pdfFile.getAbsolutePath(), duration);
                return pdfFile.getAbsolutePath();
            }
            log.error("【隔离模式转换】失败 exitCode={}, source={}",
                    process.exitValue(), officeFile.getAbsolutePath());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("【隔离模式转换】线程被中断: {}", officeFile.getAbsolutePath());
            return null;
        } catch (Exception e) {
            log.error("【隔离模式转换】异常: {}", officeFile.getAbsolutePath(), e);
            return null;
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
            if (acquired) ISOLATED_CONVERSION_SLOTS.release();
            deleteTemporaryDirectory(profileDir);
            if (processLog.exists() && !processLog.delete()) processLog.deleteOnExit();
        }
    }

    static boolean requiresIsolatedCli(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.endsWith(".ppt") || lower.endsWith(".pptx")
                || lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    static String libreOfficeUserProfileUri(File directory) {
        String normalized = directory.getAbsolutePath().replace('\\', '/');
        if (normalized.matches("^[A-Za-z]:/.*")) return "file:///" + normalized;
        return directory.toURI().toASCIIString();
    }

    private static void deleteTemporaryDirectory(File directory) {
        if (directory == null || !directory.exists()) return;
        File[] children = directory.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) deleteTemporaryDirectory(child);
                else if (!child.delete()) child.deleteOnExit();
            }
        }
        if (!directory.delete()) directory.deleteOnExit();
    }

    /**
     * 只在读锁内短暂取 converter 引用，实际 convert 在锁外执行。
     * 若 convert 全程持有读锁，高压时健康巡检拿不到写锁，会阻塞数分钟甚至更久。
     * 重建会 stop/kill 进程，在途 convert 会抛服务级异常并由上层重试或失败收口。
     */
    private static boolean executeConversionWithSharedLock(File docxFile, File pdfFile) throws OfficeException {
        DocumentConverter converter;
        OFFICE_READ_LOCK.lock();
        try {
            if (!serviceAvailable || documentConverter == null || !isManagerRunningInternal()) {
                return false;
            }
            converter = documentConverter;
        } finally {
            OFFICE_READ_LOCK.unlock();
        }
        converter.convert(docxFile).to(pdfFile).execute();
        return true;
    }

    private static boolean ensureOfficeManagerReady() {
        int processCount = countOfficeProcesses();
        if (processWarnThreshold > 0 && processCount > processWarnThreshold) {
            log.warn("【LibreOffice服务】进程数 {} 超过阈值 {}，执行受控清理重启", processCount, processWarnThreshold);
            return cleanupAndRestart("进程数超过阈值");
        }
        if (isServiceAvailable()) {
            // 进程数偏少可能是启动/重启毛刺，也可能是半死。
            // 只有明确证据（在途挂起、失败信号）才在转换前重建，避免正常排队时误杀整池。
            // 同时不再用 officeInstanceCount >= 3 这个硬编码门槛：130 上实例数为 2，
            // 该分支永远不会生效，等于形同虚设；改用与健康判定一致的证据条件。
            int expectedMin = officeInstanceCount;
            if (processCount > 0 && processCount < expectedMin
                    && !awaitingPostRebuildSuccess
                    && (consecutiveServiceFailures.get() >= 1 || inFlightConversions.get() > 0)) {
                log.warn("【LibreOffice服务】表面可用但进程偏少 process={}/instances={}（有失败或在途证据），转换前受控重建",
                        processCount, officeInstanceCount);
                return cleanupAndRestart("转换前发现进程严重偏少");
            }
            return true;
        }
        return tryRebuildIfNeeded("转换前发现服务不可用");
    }

    private static boolean tryRebuildIfNeeded(String reason) {
        boolean unavailable = !isServiceAvailable();
        int failures = consecutiveServiceFailures.get();
        if (!unavailable && failures < failureThreshold) {
            log.debug("【LibreOffice服务】暂不重建：failures={}/{}, available=true, reason={}",
                    failures, failureThreshold, reason);
            return false;
        }
        return rebuildOfficeManager(reason);
    }

    private static boolean isServiceLevelFailure(OfficeException exception) {
        if (!isServiceAvailable()) {
            return true;
        }
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }

        String normalizedMessage = message.toLowerCase(Locale.ROOT);
        return normalizedMessage.contains("office manager")
                || normalizedMessage.contains("office process")
                || normalizedMessage.contains("could not establish connection")
                || normalizedMessage.contains("connection")
                || normalizedMessage.contains("disconnected")
                || normalizedMessage.contains("pipe")
                || normalizedMessage.contains("socket")
                || normalizedMessage.contains("timeout")
                || normalizedMessage.contains("cancelled")
                || normalizedMessage.contains("canceled");
    }

    private static String truncateMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "unknown";
        }
        return message.length() <= 120 ? message : message.substring(0, 120);
    }

    /**
     * 检查 LibreOffice 是否已安装。
     */
    public static boolean isLibreOfficeInstalled() {
        File officeHome = new File(libreOfficeHome);
        return officeHome.exists() && officeHome.isDirectory();
    }

    /**
     * 检查服务是否可用。
     */
    public static boolean isServiceAvailable() {
        OFFICE_READ_LOCK.lock();
        try {
            return serviceAvailable && isManagerRunningInternal();
        } finally {
            OFFICE_READ_LOCK.unlock();
        }
    }

    private static boolean isManagerRunningInternal() {
        return officeManager != null && officeManager.isRunning();
    }

    /**
     * 定时维护入口：停止本应用管理的服务池、清理残留进程并重启。
     */
    public static String cleanupAndRestartForMaintenance() {
        boolean success = cleanupAndRestart("定时维护");
        return success ? "LibreOffice 服务清理并重启成功" : "LibreOffice 服务清理后重启失败";
    }

    /**
     * 分钟级健康巡检入口：仅在判定不健康且冷却结束后执行 cleanup + 重启。
     *
     * @return 结果快照，含 healthy / recovered / skipped / reason 等字段，供 Quartz 与诊断使用
     */
    public static Map<String, Object> healthCheckAndRecover() {
        long now = System.currentTimeMillis();
        Map<String, Object> probe = probeHealth(now);
        boolean healthy = Boolean.TRUE.equals(probe.get("healthy"));
        String reason = String.valueOf(probe.get("unhealthyReason"));

        Map<String, Object> result = new LinkedHashMap<>(probe);
        result.put("recovered", Boolean.FALSE);
        result.put("skipped", Boolean.FALSE);
        result.put("action", "none");

        log.info("【LibreOffice自愈】巡检 healthy={} reason={} process={} inFlight={} hangMs={} sinceSuccessMs={} failures={} cooldown={}",
                healthy, reason, probe.get("processCount"), probe.get("inFlightConversions"),
                probe.get("oldestInFlightHangMs"), probe.get("sinceLastSuccessMs"),
                probe.get("consecutiveServiceFailures"), probe.get("inRebuildCooldown"));

        if (healthy) {
            result.put("message", "LibreOffice 健康检查通过");
            return result;
        }

        if (isInRebuildCooldown(now)) {
            // 退避期间的重启间隔会随“连续无成功重建次数”成倍拉长，
            // 用于打断“每 3～7 分钟重启一次整池”的循环。
            long remainMs = effectiveCooldownMs() - (now - lastRebuildAtMs);
            result.put("skipped", Boolean.TRUE);
            result.put("action", "cooldown");
            result.put("cooldownRemainMs", Math.max(remainMs, 0L));
            result.put("effectiveCooldownMs", effectiveCooldownMs());
            result.put("rebuildsWithoutSuccess", rebuildsWithoutSuccess);
            result.put("message", "LibreOffice 不健康但处于重建冷却中: " + reason);
            log.warn("【LibreOffice自愈】跳过重建（冷却中），原因={}，剩余约{}ms（本次冷却{}ms，连续无成功重建={}）",
                    reason, Math.max(remainMs, 0L), effectiveCooldownMs(), rebuildsWithoutSuccess);
            return result;
        }

        log.warn("【LibreOffice自愈】触发清理重启，原因={}", reason);
        boolean success = cleanupAndRestart("健康巡检:" + reason);
        result.put("recovered", success);
        result.put("action", success ? "cleanupAndRestart" : "cleanupAndRestartFailed");
        result.put("message", success
                ? "LibreOffice 不健康已清理并重启: " + reason
                : "LibreOffice 清理重启失败: " + reason);
        // 刷新探测结果供调用方观察重启后的即时状态；
        // 注意观察期判定会在重启后立即生效，因此这里通常仍显示不健康，属于预期行为。
        result.putAll(probeHealth(System.currentTimeMillis()));
        result.put("recovered", success);
        result.put("message", success
                ? "LibreOffice 不健康已清理并重启: " + reason
                : "LibreOffice 清理重启失败: " + reason);
        return result;
    }

    /**
     * 探测当前健康状态（不执行重启）。供诊断与单元测试使用。
     */
    public static Map<String, Object> probeHealth() {
        return probeHealth(System.currentTimeMillis());
    }

    static Map<String, Object> probeHealth(long now) {
        int processCount = countOfficeProcesses();
        boolean available = isServiceAvailable();
        int inFlight = inFlightConversions.get();
        long oldestStart = oldestInFlightStartMs.get();
        long hangMs = (inFlight > 0 && oldestStart > 0L) ? Math.max(now - oldestStart, 0L) : 0L;
        int consecutiveFailures = consecutiveServiceFailures.get();

        long sinceSuccessMs = lastSuccessAtMs > 0L ? Math.max(now - lastSuccessAtMs, 0L) : -1L;
        String unhealthyReason = evaluateUnhealthyReason(
                available, processCount, processWarnThreshold, officeInstanceCount,
                consecutiveFailures, failureThreshold, inFlight, hangMs, softHangTimeoutMs,
                sinceSuccessMs);
        // 重建后观察期：重建过、但至今没有一次真实成功转换，且已超过软挂起超时。
        // 旧实现把在途与失败计数一起清零，于是下一轮巡检 inFlight=0、failures=0 直接报 healthy=true，
        // 而卡死的旧线程与毒文件仍在，重试立刻再次打崩整池——这就是 21:36～21:43 连续 8 分钟
        // sinceSuccessMs 一路涨到 467965 却报健康的原因。此判定不受计数是否被清零影响。
        long sinceRebuildMs = lastRebuildAtMs > 0L ? Math.max(now - lastRebuildAtMs, 0L) : -1L;
        if (unhealthyReason == null
                && isPostRebuildObservationUnhealthy(awaitingPostRebuildSuccess, sinceRebuildMs,
                        softHangTimeoutMs, conversionAttemptsSinceRebuild.get())) {
            unhealthyReason = "重建后长时间无成功转换(" + sinceRebuildMs
                    + "ms, 尝试=" + conversionAttemptsSinceRebuild.get()
                    + ", 连续无成功重建=" + rebuildsWithoutSuccess + ")";
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("healthy", unhealthyReason == null);
        data.put("unhealthyReason", unhealthyReason == null ? "" : unhealthyReason);
        data.put("serviceAvailable", available);
        data.put("processCount", processCount);
        data.put("processWarnThreshold", processWarnThreshold);
        data.put("instanceCount", officeInstanceCount);
        data.put("inFlightConversions", inFlight);
        data.put("oldestInFlightHangMs", hangMs);
        data.put("softHangTimeoutMs", softHangTimeoutMs);
        data.put("consecutiveServiceFailures", consecutiveFailures);
        data.put("failureThreshold", failureThreshold);
        data.put("rebuildCooldownMs", rebuildCooldownMs);
        data.put("inRebuildCooldown", isInRebuildCooldown(now));
        data.put("lastSuccessAtMs", lastSuccessAtMs);
        data.put("lastFailureAtMs", lastFailureAtMs);
        data.put("lastRebuildAtMs", lastRebuildAtMs);
        data.put("lastRebuildReason", lastRebuildReason);
        data.put("sinceLastSuccessMs", sinceSuccessMs);
        return data;
    }

    /**
     * 纯判定：重建后的观察期是否应判定为不健康。
     * <p>
     * 关键回归点：重建会把在途计数与失败计数清零，旧逻辑因此立刻报 healthy=true，
     * 而卡死的线程与毒文件仍在，重试随即再次打崩整池——这就是 130 上
     * 21:36～21:43 连续 8 分钟零成功转换却报健康的原因。
     * 本判定不依赖那两个被清零的计数，只看"重建过 + 仍在无成功 + 期间确有转换尝试"。
     *
     * @param attemptsSinceRebuild 重建后已发起的转换次数；为 0 说明只是空闲，不能判为不健康
     */
    static boolean isPostRebuildObservationUnhealthy(boolean awaitingPostRebuildSuccess,
                                                     long sinceRebuildMs,
                                                     long softHangTimeoutMs,
                                                     int attemptsSinceRebuild) {
        return awaitingPostRebuildSuccess
                && softHangTimeoutMs > 0
                && sinceRebuildMs >= softHangTimeoutMs
                && attemptsSinceRebuild > 0;
    }

    /**
     * 纯判定逻辑，便于单测。返回 null 表示健康，否则为不健康原因。
     */
    static String evaluateUnhealthyReason(boolean serviceAvailable,
                                          int processCount,
                                          int processWarnThreshold,
                                          int instanceCount,
                                          int consecutiveFailures,
                                          int failureThreshold,
                                          int inFlight,
                                          long hangMs,
                                          long softHangTimeoutMs) {
        return evaluateUnhealthyReason(serviceAvailable, processCount, processWarnThreshold, instanceCount,
                consecutiveFailures, failureThreshold, inFlight, hangMs, softHangTimeoutMs, -1L);
    }

    /**
     * @param sinceSuccessMs 距最近一次转换成功的毫秒；&lt;0 表示尚无成功记录
     */
    static String evaluateUnhealthyReason(boolean serviceAvailable,
                                          int processCount,
                                          int processWarnThreshold,
                                          int instanceCount,
                                          int consecutiveFailures,
                                          int failureThreshold,
                                          int inFlight,
                                          long hangMs,
                                          long softHangTimeoutMs,
                                          long sinceSuccessMs) {
        if (processWarnThreshold > 0 && processCount > processWarnThreshold) {
            return "进程数超过阈值(" + processCount + ">" + processWarnThreshold + ")";
        }
        if (!serviceAvailable) {
            return "服务不可用";
        }
        // 管理器声称可用但系统中已无 soffice，典型半死/已崩
        if (processCount <= 0) {
            return "服务标记可用但无 soffice 进程";
        }
        if (consecutiveFailures >= failureThreshold) {
            return "连续服务失败达到阈值(" + consecutiveFailures + ">=" + failureThreshold + ")";
        }
        // 在途挂起：最老任务过久且近期无成功（避免高压排队误杀）
        if (inFlight > 0 && softHangTimeoutMs > 0 && hangMs >= softHangTimeoutMs) {
            boolean noRecentSuccess = sinceSuccessMs < 0L || sinceSuccessMs >= softHangTimeoutMs;
            if (noRecentSuccess) {
                return "在途转换挂起超时(" + hangMs + "ms, inFlight=" + inFlight + ")";
            }
        }
        // 半死增强：服务仍标记可用、进程还在，但超过软超时无任何成功，
        // 且已出现过失败信号。覆盖「convert 卡死但 inFlight 计数被重建清零」场景。
        if (serviceAvailable && processCount > 0 && softHangTimeoutMs > 0
                && sinceSuccessMs >= softHangTimeoutMs
                && consecutiveFailures >= 1) {
            return "服务表面可用但持续无成功转换(" + sinceSuccessMs + "ms)";
        }
        // 进程严重偏少：countOfficeProcesses 含 exe+bin，健康时约 2*instanceCount。
        // 少于 instanceCount 即视为半死；需附加 inFlight/失败/无成功 信号，避免启动毛刺误杀。
        if (instanceCount >= 3 && processCount > 0 && processCount < instanceCount) {
            boolean hasFailureSignal = consecutiveFailures >= 1;
            boolean hasStuckWork = inFlight > 0 && hangMs >= Math.min(softHangTimeoutMs, 30_000L);
            boolean noSuccessLong = softHangTimeoutMs > 0 && sinceSuccessMs >= softHangTimeoutMs;
            if (hasFailureSignal || hasStuckWork || noSuccessLong) {
                return "进程数严重偏少(process=" + processCount + "/instances=" + instanceCount
                        + ", failures=" + consecutiveFailures + ", inFlight=" + inFlight + ")";
            }
        }
        return null;
    }

    public static Map<String, Object> getHealthSnapshot() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> probe = probeHealth();
        data.putAll(probe);
        data.put("installed", isLibreOfficeInstalled());
        data.put("officeHome", libreOfficeHome);
        data.put("excessiveProcesses", processWarnThreshold > 0
                && Integer.parseInt(String.valueOf(probe.get("processCount"))) > processWarnThreshold);
        data.put("serviceFailureCount", serviceFailureCount.get());
        return data;
    }

    public static int getOfficeInstanceCount() {
        return officeInstanceCount;
    }

    public static int countOfficeProcesses() {
        return countProcessByName("soffice.exe") + countProcessByName("soffice.bin");
    }

    private static boolean cleanupAndRestart(String reason) {
        long now = System.currentTimeMillis();
        // 日级维护无条件执行；健康巡检/转换路径在冷却期跳过，避免抖动
        boolean force = reason != null && reason.contains("定时维护");
        if (!force && isInRebuildCooldown(now)) {
            log.warn("【LibreOffice服务】跳过清理重启（冷却中），原因={}", reason);
            return false;
        }
        if (!acquireWriteLockForRecovery("清理重启:" + reason)) {
            return false;
        }
        try {
            if (!force && isInRebuildCooldown(System.currentTimeMillis())
                    && isManagerRunningInternal() && serviceAvailable) {
                log.info("【LibreOffice服务】他线程已完成清理重启，跳过，原因={}", reason);
                return true;
            }
            log.warn("【LibreOffice服务】开始清理重启，原因={}", reason);
            int interrupted = interruptStuckConversions(0L, softHangTimeoutMs);
            if (interrupted > 0) {
                log.warn("【LibreOffice服务】清理重启前中断卡死转换线程 {} 个", interrupted);
            }
            stopOfficeManagerInternal(true);
            waitForOfficeShutdown();
            // stop 内已杀过一次；再扫一遍兜底
            killOrphanedOfficeProcesses();
            boolean started = startOfficeManagerInternal(false);
            markRebuild(reason, started);
            return started;
        } finally {
            OFFICE_WRITE_LOCK.unlock();
        }
    }

    /**
     * 获取写锁以便整池重建。
     * 若转换线程因半死挂起长期持有读锁，先杀 soffice 打断 convert，再限时抢写锁，
     * 避免分钟级健康巡检在 writeLock.lock() 上永久阻塞。
     */
    private static boolean acquireWriteLockForRecovery(String action) {
        if (OFFICE_WRITE_LOCK.tryLock()) {
            return true;
        }
        log.warn("【LibreOffice服务】{} 写锁被占用，先强制清理 soffice 以打断挂起转换", action);
        // 先标记不可用，减少新请求继续进入 convert
        serviceAvailable = false;
        killOrphanedOfficeProcesses();
        try {
            // 转换已不再长时间持有读锁；此处限时等待仅作兜底
            if (OFFICE_WRITE_LOCK.tryLock(15, TimeUnit.SECONDS)) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("【LibreOffice服务】{} 等待写锁被中断", action);
            return false;
        }
        log.error("【LibreOffice服务】{} 等待写锁超时，本轮自愈放弃，下轮巡检将重试", action);
        // 记录一次失败信号，便于后续 soft-hang / 连续失败继续触发
        markServiceFailure("写锁超时:" + action);
        lastRebuildReason = "写锁超时:" + action;
        return false;
    }

    private static void markRebuild(String reason, boolean started) {
        long now = System.currentTimeMillis();
        lastRebuildAtMs = now;
        lastRebuildReason = reason == null ? "" : reason;
        // 重建后必须等一次真实成功转换才算恢复。
        // 旧实现直接清零在途与失败计数，导致下一轮巡检看到 inFlight=0、failures=0 就报 healthy=true，
        // 而卡死的旧线程其实还在（毒文件还会被重试捞回），形成“假恢复 + 反复整池重启”。
        awaitingPostRebuildSuccess = true;
        conversionAttemptsSinceRebuild.set(0);
        if (started) {
            rebuildsWithoutSuccess++;
            consecutiveServiceFailures.set(0);
            serviceFailureCount.set(0);
        } else {
            // 重建失败本身也是不健康证据，保留失败计数以免被当成健康
            rebuildsWithoutSuccess++;
            markServiceFailure("重建失败:" + reason);
        }
        log.warn("【LibreOffice服务】重建完成 started={} 连续无成功重建次数={}", started, rebuildsWithoutSuccess);
    }

    /**
     * 重建退避：连续重建且期间没有一次成功转换时，逐步拉长冷却，避免每 3～7 分钟重启一次整池。
     * 只要出现一次成功转换，立即恢复到基础冷却。
     */
    private static long effectiveCooldownMs() {
        return effectiveCooldownMs(rebuildCooldownMs, rebuildsWithoutSuccess);
    }

    /**
     * 纯计算：连续重建且期间无成功转换时，冷却按 2 倍逐级拉长，上限 30 分钟。
     * 抽成静态纯函数便于单测；用于打断"每 3～7 分钟重启一次整池"的循环。
     */
    static long effectiveCooldownMs(long baseCooldownMs, int rebuildsWithoutSuccess) {
        int n = Math.min(Math.max(rebuildsWithoutSuccess, 0), 3);
        long cooldown = baseCooldownMs;
        for (int i = 0; i < n; i++) {
            cooldown = Math.min(cooldown * 2L, 30L * 60L * 1000L);
        }
        return cooldown;
    }

    private static boolean isInRebuildCooldown(long now) {
        return lastRebuildAtMs > 0L && (now - lastRebuildAtMs) < effectiveCooldownMs();
    }

    private static void markServiceSuccess() {
        lastSuccessAtMs = System.currentTimeMillis();
        consecutiveServiceFailures.set(0);
        serviceFailureCount.set(0);
        // 出现真实成功才算恢复：解除重建后观察期并清空退避
        awaitingPostRebuildSuccess = false;
        rebuildsWithoutSuccess = 0;
        conversionAttemptsSinceRebuild.set(0);
    }

    private static void markServiceFailure(String reason) {
        lastFailureAtMs = System.currentTimeMillis();
        consecutiveServiceFailures.incrementAndGet();
        // 提升到 info：生产默认 INFO 级别下 debug 不落盘，故障时无法从日志判断计数来源
        log.info("【LibreOffice服务】记录失败信号: {}", reason);
    }

    /**
     * 仅记录文档级失败，不计入服务失败计数。
     * 单份坏文档不应该让整池被判为不健康并触发重启；
     * 服务是否健康由进程存活、在途挂起、服务级异常共同决定。
     */
    private static void markDocumentFailureOnly() {
        lastFailureAtMs = System.currentTimeMillis();
    }

    /**
     * 毒文件登记：记录某源文件把共享池打崩的次数，达到阈值后改走隔离进程。
     *
     * @return 该文件当前的命中次数
     */
    static int registerPoisonFileHit(String path) {
        if (path == null || path.isEmpty()) {
            return 0;
        }
        AtomicInteger counter = poisonFileHits.computeIfAbsent(path, key -> new AtomicInteger(0));
        int hits = counter.incrementAndGet();
        log.warn("【LibreOffice服务】源文件触发共享池崩溃，命中次数={} 文件={}", hits, path);
        return hits;
    }

    /**
     * 该文件是否应改走一次性隔离进程（隔离进程不做刷新，也不会拖垮共享池）。
     */
    static boolean isPoisonFileIsolated(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        AtomicInteger counter = poisonFileHits.get(path);
        return counter != null && counter.get() >= poisonIsolateThreshold;
    }

    static int getPoisonFileHitCount(String path) {
        AtomicInteger counter = path == null ? null : poisonFileHits.get(path);
        return counter == null ? 0 : counter.get();
    }

    static Map<String, Integer> getPoisonFileSnapshot() {
        Map<String, Integer> snapshot = new LinkedHashMap<>();
        poisonFileHits.forEach((path, counter) -> snapshot.put(path, counter.get()));
        return snapshot;
    }

    /**
     * 登记“任何通道都转不出来”的源文件，避免无限重试反复拖垮整池。
     * 仅在隔离通道也失败后调用；只记录，绝不删除学生原始文件。
     */
    static void markUnconvertible(String path, String reason) {
        if (path == null || path.isEmpty()) {
            return;
        }
        String previous = unconvertibleFiles.putIfAbsent(path, reason);
        if (previous == null) {
            log.error("【LibreOffice服务】登记不可转换文件（保留原文件，停止自动重试）: {} 原因={}", path, reason);
        }
    }

    /** 该源文件是否已被判定为任何通道都无法转换 */
    public static boolean isUnconvertible(String path) {
        return path != null && !path.isEmpty() && unconvertibleFiles.containsKey(path);
    }

    /**
     * 该源文件是否已被判定为任何通道都无法转换（按路径查询，供转换前拦截）。
     */
    public static boolean isPersistentlyUnconvertible(String path) {
        return isUnconvertible(path);
    }

    /**
     * 落库的“不可转换”失败原因文本。
     * 内存登记表 {@link #unconvertibleFiles} 会随后端重启清空，而答题行里的
     * preview_error_message 不会；重试链路又恰好会先把它清成 NULL。
     * 因此把它作为持久化标记：重启后仍能识别并拒绝再次转换同一毒文件，
     * 避免教师在失败后手动点“重转”把整池再打崩一轮。
     */
    public static final String UNCONVERTIBLE_MESSAGE =
            "该文件无法转换为预览（文件内容异常，已隔离并停止自动重试，请重新导出后提交）";

    /**
     * 判断答题行上的失败原因是否就是“不可转换”标记。
     * 用于重试领取前拦截：自动重试靠 preview_retry_count 打满即可挡住，
     * 但教师手动重转会传 ignoreRetryLimit=true 绕过计数上限，必须靠这里的文本判定兜住。
     */
    public static boolean isUnconvertibleMessage(String errorMessage) {
        return errorMessage != null && errorMessage.contains(UNCONVERTIBLE_MESSAGE);
    }

    /** 不可转换的原因说明，用于回写教师可见的失败原因 */
    public static String getUnconvertibleReason(String path) {
        return path == null ? null : unconvertibleFiles.get(path);
    }

    static Map<String, String> getUnconvertibleSnapshot() {
        return new LinkedHashMap<>(unconvertibleFiles);
    }

    private static void beginInFlight(long startTime, String filePath) {
        conversionAttemptsSinceRebuild.incrementAndGet();
        inFlightConversions.incrementAndGet();
        oldestInFlightStartMs.compareAndSet(0L, startTime);
        inFlightSlots.put(Thread.currentThread(),
                new InFlightSlot(startTime, inFlightEpoch.get(), filePath));
    }

    private static void endInFlight() {
        InFlightSlot slot = inFlightSlots.remove(Thread.currentThread());
        // 世代不符说明本线程属于重建之前的那一轮，其结果不能用来修改当前在途计数
        if (slot != null && slot.epoch != inFlightEpoch.get()) {
            log.debug("【LibreOffice服务】丢弃跨重建的在途计数回收，线程={}", Thread.currentThread().getName());
            return;
        }
        int left = inFlightConversions.decrementAndGet();
        // 重建可能已清零计数；避免减成负数干扰后续挂起判定
        if (left <= 0) {
            inFlightConversions.set(0);
            oldestInFlightStartMs.set(0L);
        }
    }

    /**
     * 重建前重置在途跟踪。
     * 进入新一轮世代，让仍在锁外执行的旧 convert 线程无法再影响新计数；
     * 同时保留它们的时间证据（写入 oldestInFlightStartMs 由新老判定使用），
     * 不做“假装什么都没发生”的静默清零。
     */
    private static void clearInFlightTracking() {
        inFlightEpoch.incrementAndGet();
        inFlightConversions.set(0);
        oldestInFlightStartMs.set(0L);
        inFlightSlots.clear();
    }

    /**
     * 中断卡死的转换线程。
     * 旧实现只杀 soffice 进程，卡在 convert 里的 Java 线程仍然是僵尸并继续占用
     * conversionExecutor 线程（130 上池只有 2 个线程，两个卡死即全池瘫痪）。
     * <p>
     * 注意：进程被杀后所有在途转换都会失败，因此本方法会中断全部在途线程；
     * 但只有“已经卡超过软挂起超时”的线程，其文件才被判为毒文件——
     * 否则一次整池重建会把当时所有正常排队的文件都误判成毒文件。
     *
     * @param minAgeMs 仅中断运行时长达到该值的线程；0 表示全部中断
     * @param poisonMinAgeMs 运行时长达到该值的线程，其源文件登记为毒文件；小于等于 0 表示不登记
     * @return 被中断的线程数
     */
    private static int interruptStuckConversions(long minAgeMs, long poisonMinAgeMs) {
        long now = System.currentTimeMillis();
        int interrupted = 0;
        for (Map.Entry<Thread, InFlightSlot> entry : inFlightSlots.entrySet()) {
            Thread worker = entry.getKey();
            InFlightSlot slot = entry.getValue();
            if (worker == null || slot == null || !worker.isAlive()) {
                continue;
            }
            long age = now - slot.startMs;
            if (minAgeMs > 0 && age < minAgeMs) {
                continue;
            }
            // 只有长时间卡住的文件才是崩溃诱因，短时在途属于正常排队
            if (poisonMinAgeMs > 0 && age >= poisonMinAgeMs && slot.filePath != null) {
                registerPoisonFileHit(slot.filePath);
            }
            log.warn("【LibreOffice服务】中断卡死的转换线程 线程={} 已运行={}ms 世代={} 文件={}",
                    worker.getName(), age, slot.epoch, slot.filePath);
            worker.interrupt();
            interrupted++;
        }
        return interrupted;
    }

    /** 在途转换槽位：记录开始时间、所属世代与源文件路径 */
    private static final class InFlightSlot {
        final long startMs;
        final long epoch;
        final String filePath;

        InFlightSlot(long startMs, long epoch, String filePath) {
            this.startMs = startMs;
            this.epoch = epoch;
            this.filePath = filePath;
        }
    }

    private static int countProcessByName(String processName) {
        try {
            ProcessBuilder processBuilder = isWindows()
                    ? new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + processName)
                    : new ProcessBuilder("ps", "-u", System.getProperty("user.name"), "-o", "comm=");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int count = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isWindows() ? line.toLowerCase(Locale.ROOT).startsWith(processName.toLowerCase(Locale.ROOT))
                            : line.trim().equals(processName)) {
                        count++;
                    }
                }
            }
            process.waitFor();
            return count;
        } catch (Exception e) {
            log.debug("【LibreOffice服务】统计 {} 进程失败: {}", processName, e.getMessage());
            return 0;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
    }

    static boolean isManagedLinuxOfficeProcess(String line, String processName, int[] ports) {
        String[] fields = line.trim().split("\\s+", 3);
        if (fields.length != 3 || !fields[0].matches("[0-9]+") || !fields[1].equals(processName)) return false;
        for (int port : ports) {
            if (fields[2].contains(".jodconverter_socket_host-127.0.0.1_port-" + port + "_")) return true;
        }
        return false;
    }
}
