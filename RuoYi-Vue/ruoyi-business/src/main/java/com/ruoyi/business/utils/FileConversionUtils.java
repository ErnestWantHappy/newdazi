package com.ruoyi.business.utils;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 文件转换工具类
 * 使用 JODConverter + LibreOffice 服务模式进行文档转换。
 */
@Component
public class FileConversionUtils {

    private static final Logger log = LoggerFactory.getLogger(FileConversionUtils.class);

    // LibreOffice 安装路径（Windows 默认）
    private static final String LIBRE_OFFICE_HOME = "C:\\Program Files\\LibreOffice";

    // LibreOffice 实例数需要与转换线程池大小保持一致。
    private static final int OFFICE_INSTANCE_COUNT = 5;
    private static final int MAX_TASKS_PER_PROCESS = 30;
    private static final long TASK_EXECUTION_TIMEOUT = 300_000L;
    private static final long TASK_QUEUE_TIMEOUT = 120_000L;
    private static final long PROCESS_TIMEOUT = 120_000L;
    private static final int MAX_RETRY = 1;

    private static final ReentrantReadWriteLock OFFICE_MANAGER_LOCK = new ReentrantReadWriteLock(true);
    private static final Lock OFFICE_READ_LOCK = OFFICE_MANAGER_LOCK.readLock();
    private static final Lock OFFICE_WRITE_LOCK = OFFICE_MANAGER_LOCK.writeLock();

    private static final AtomicInteger serviceFailureCount = new AtomicInteger(0);

    private static OfficeManager officeManager;
    private static DocumentConverter documentConverter;
    private static volatile boolean serviceAvailable = false;

    @PostConstruct
    public void init() {
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

            log.info("【LibreOffice服务】正在启动服务模式（{}个实例）...", OFFICE_INSTANCE_COUNT);
            int[] ports = buildPortNumbers();

            LocalOfficeManager newOfficeManager = LocalOfficeManager.builder()
                    .officeHome(LIBRE_OFFICE_HOME)
                    .portNumbers(ports)
                    .maxTasksPerProcess(MAX_TASKS_PER_PROCESS)
                    .taskExecutionTimeout(TASK_EXECUTION_TIMEOUT)
                    .taskQueueTimeout(TASK_QUEUE_TIMEOUT)
                    .processTimeout(PROCESS_TIMEOUT)
                    .build();

            newOfficeManager.start();
            officeManager = newOfficeManager;
            documentConverter = LocalConverter.builder()
                    .officeManager(newOfficeManager)
                    .build();
            serviceAvailable = true;
            serviceFailureCount.set(0);
            log.info("【LibreOffice服务】服务启动成功，支持{}个并发转换实例", OFFICE_INSTANCE_COUNT);
            return true;
        } catch (Exception e) {
            officeManager = null;
            documentConverter = null;
            serviceAvailable = false;
            log.error("【LibreOffice服务】启动失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private static void stopOfficeManagerInternal(boolean killAfterStopFailure) {
        OfficeManager currentOfficeManager = officeManager;
        officeManager = null;
        documentConverter = null;
        serviceAvailable = false;

        if (currentOfficeManager == null) {
            return;
        }

        try {
            currentOfficeManager.stop();
            log.info("【LibreOffice服务】服务已停止");
        } catch (OfficeException e) {
            log.warn("【LibreOffice服务】服务停止异常: {}", e.getMessage(), e);
            if (killAfterStopFailure) {
                killOrphanedOfficeProcesses();
            }
        }
    }

    private static boolean rebuildOfficeManager(String reason) {
        OFFICE_WRITE_LOCK.lock();
        try {
            log.warn("【LibreOffice服务】开始重建，原因={}", reason);
            stopOfficeManagerInternal(true);
            waitForOfficeShutdown();
            boolean started = startOfficeManagerInternal(false);
            if (!started) {
                log.error("【LibreOffice服务】重建失败，原因={}", reason);
            }
            return started;
        } finally {
            OFFICE_WRITE_LOCK.unlock();
        }
    }

    private static int[] buildPortNumbers() {
        int[] ports = new int[OFFICE_INSTANCE_COUNT];
        for (int i = 0; i < OFFICE_INSTANCE_COUNT; i++) {
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
     * 仅在首次启动前或优雅关闭失败后的兜底场景使用。
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
        File docxFile = new File(docxFilePath);
        if (!docxFile.exists()) {
            log.error("源文件不存在: {}", docxFilePath);
            return null;
        }

        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            log.error("输出目录创建失败: {}", outputDir);
            return null;
        }

        String docxFileName = docxFile.getName();
        String pdfFileName = docxFileName.replaceAll("(?i)\\.docx?$", ".pdf");
        String pdfFilePath = outputDir + File.separator + pdfFileName;
        File pdfFile = new File(pdfFilePath);

        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            if (!ensureOfficeManagerReady()) {
                log.error("【LibreOffice服务】服务不可用，无法进行转换: {}", docxFilePath);
                return null;
            }

            long startTime = System.currentTimeMillis();
            try {
                if (!executeConversionWithSharedLock(docxFile, pdfFile)) {
                    if (attempt < MAX_RETRY && rebuildOfficeManager("服务未就绪")) {
                        continue;
                    }
                    log.error("【服务模式转换】服务未就绪，转换失败: {}", docxFilePath);
                    return null;
                }

                long duration = System.currentTimeMillis() - startTime;
                if (pdfFile.exists()) {
                    log.info("【服务模式转换】成功: {} -> {} (耗时: {}ms)", docxFilePath, pdfFilePath, duration);
                    serviceFailureCount.set(0);
                    return pdfFilePath;
                }

                log.error("【服务模式转换】完成但 PDF 文件未生成: {}", pdfFilePath);
                return null;
            } catch (OfficeException e) {
                if (!isServiceLevelFailure(e)) {
                    log.error("【服务模式转换】文档转换失败，不触发服务重建: {}", e.getMessage(), e);
                    return null;
                }

                int failureCount = serviceFailureCount.incrementAndGet();
                String rebuildReason = "服务级异常#" + failureCount + ": " + truncateMessage(e.getMessage());
                log.warn("【服务模式转换】检测到服务级异常，将尝试重建: {}", rebuildReason, e);

                if (attempt < MAX_RETRY && rebuildOfficeManager(rebuildReason)) {
                    continue;
                }

                log.error("【服务模式转换】服务级异常重试失败: {}", docxFilePath);
                return null;
            }
        }

        log.error("【服务模式转换】所有重试均失败: {}", docxFilePath);
        return null;
    }

    private static boolean executeConversionWithSharedLock(File docxFile, File pdfFile) throws OfficeException {
        OFFICE_READ_LOCK.lock();
        try {
            if (!serviceAvailable || documentConverter == null || !isManagerRunningInternal()) {
                return false;
            }
            documentConverter.convert(docxFile).to(pdfFile).execute();
            return true;
        } finally {
            OFFICE_READ_LOCK.unlock();
        }
    }

    private static boolean ensureOfficeManagerReady() {
        if (isServiceAvailable()) {
            return true;
        }
        return rebuildOfficeManager("转换前发现服务不可用");
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
                || normalizedMessage.contains("socket");
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
        File officeHome = new File(LIBRE_OFFICE_HOME);
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
}
