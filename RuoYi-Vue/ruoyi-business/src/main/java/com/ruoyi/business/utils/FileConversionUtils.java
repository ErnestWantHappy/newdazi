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
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 文件转换工具类
 * 使用 JODConverter + LibreOffice 服务模式进行文档转换
 * 
 * 针对 Windows 服务器长时间运行优化：
 * 1. 自动检测 LibreOffice 进程健康状态
 * 2. 超时/挂死时自动强制重启
 * 3. 连续失败计数器，达到阈值时强制清理所有进程重建
 * 4. 转换任务支持重试机制
 */
@Component
public class FileConversionUtils {

    private static final Logger log = LoggerFactory.getLogger(FileConversionUtils.class);

    // LibreOffice 安装路径（Windows默认）
    private static final String LIBRE_OFFICE_HOME = "C:\\Program Files\\LibreOffice";

    // === 关键参数配置 ===
    // LibreOffice 实例数（端口数），与线程池大小保持一致
    // 全县级平台，操作题可能有多个班级同时提交，设为 5 个实例
    private static final int OFFICE_INSTANCE_COUNT = 5;
    // 每个进程处理任务数后自动重启（防止内存泄漏）
    private static final int MAX_TASKS_PER_PROCESS = 30;
    // 单个任务执行超时（5分钟，适应大文件）
    private static final long TASK_EXECUTION_TIMEOUT = 300_000L;
    // 队列等待超时（2分钟）
    private static final long TASK_QUEUE_TIMEOUT = 120_000L;
    // 进程启动超时（2分钟，Windows 上 LibreOffice 启动较慢）
    private static final long PROCESS_TIMEOUT = 120_000L;
    // 连续失败次数达到此阈值时强制重建
    private static final int FAILURE_THRESHOLD = 3;
    // 最大重试次数
    private static final int MAX_RETRY = 1;

    // 服务管理器
    private static OfficeManager officeManager;
    
    // 文档转换器
    private static DocumentConverter documentConverter;
    
    // 服务是否可用
    private static volatile boolean serviceAvailable = false;

    // 连续失败计数器
    private static final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    // 上次成功重启时间（防止短时间内反复重启）
    private static final AtomicLong lastRestartTime = new AtomicLong(0);

    // 最小重启间隔（30秒）
    private static final long MIN_RESTART_INTERVAL = 30_000L;

    /**
     * 应用启动时初始化 LibreOffice 服务
     */
    @PostConstruct
    public void init() {
        startOfficeManager();
    }

    /**
     * 应用关闭时停止服务
     */
    @PreDestroy
    public void destroy() {
        stopOfficeManager();
    }

    /**
     * 启动 LibreOffice 服务管理器
     */
    private static synchronized void startOfficeManager() {
        if (officeManager != null && officeManager.isRunning()) {
            log.info("【LibreOffice服务】服务已在运行中");
            return;
        }

        try {
            log.info("【LibreOffice服务】正在启动服务模式（{}个实例）...", OFFICE_INSTANCE_COUNT);
            
            // 先清理可能残留的 LibreOffice 进程
            killOrphanedOfficeProcesses();

            // 构建端口号数组
            int[] ports = new int[OFFICE_INSTANCE_COUNT];
            for (int i = 0; i < OFFICE_INSTANCE_COUNT; i++) {
                ports[i] = 2002 + i;
            }

            // 创建本地办公套件管理器
            officeManager = LocalOfficeManager.builder()
                .officeHome(LIBRE_OFFICE_HOME)
                .portNumbers(ports)
                .maxTasksPerProcess(MAX_TASKS_PER_PROCESS)
                .taskExecutionTimeout(TASK_EXECUTION_TIMEOUT)
                .taskQueueTimeout(TASK_QUEUE_TIMEOUT)
                .processTimeout(PROCESS_TIMEOUT)
                .build();

            officeManager.start();
            
            // 创建文档转换器
            documentConverter = LocalConverter.builder()
                .officeManager(officeManager)
                .build();

            serviceAvailable = true;
            consecutiveFailures.set(0);
            lastRestartTime.set(System.currentTimeMillis());
            log.info("【LibreOffice服务】服务启动成功，支持{}个并发转换实例", OFFICE_INSTANCE_COUNT);
            
        } catch (Exception e) {
            log.error("【LibreOffice服务】启动失败: {}", e.getMessage(), e);
            serviceAvailable = false;
        }
    }

    /**
     * 停止 LibreOffice 服务管理器
     */
    private static synchronized void stopOfficeManager() {
        if (officeManager != null) {
            // 先主动杀掉 soffice 进程，避免 stop() 时等待 2 分钟超时
            killOrphanedOfficeProcesses();
            try {
                officeManager.stop();
                log.info("【LibreOffice服务】服务已停止");
            } catch (OfficeException e) {
                log.warn("【LibreOffice服务】停止时出现异常（进程已被提前清理，可忽略）: {}", e.getMessage());
            }
            officeManager = null;
            documentConverter = null;
            serviceAvailable = false;
        }
    }

    /**
     * 强制重建 LibreOffice 服务
     * 停止现有服务 → 清理残留进程 → 重新启动
     */
    private static synchronized void forceRebuildOfficeManager() {
        long now = System.currentTimeMillis();
        if (now - lastRestartTime.get() < MIN_RESTART_INTERVAL) {
            log.warn("【LibreOffice服务】距离上次重启不足30秒，跳过本次重建");
            return;
        }

        log.warn("【LibreOffice服务】开始强制重建...");
        stopOfficeManager();

        // 等待进程完全退出
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        startOfficeManager();
    }

    /**
     * 清理残留的 LibreOffice / soffice 进程（Windows）
     * 避免僵尸进程占用端口
     */
    private static void killOrphanedOfficeProcesses() {
        try {
            log.info("【LibreOffice服务】清理残留 soffice 进程...");
            // Windows 下强制结束 soffice.bin 进程
            ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", "soffice.bin");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();
            
            // 也清理 soffice.exe
            pb = new ProcessBuilder("taskkill", "/F", "/IM", "soffice.exe");
            pb.redirectErrorStream(true);
            p = pb.start();
            p.waitFor();

            // 等待进程退出
            Thread.sleep(2000);
            log.info("【LibreOffice服务】残留进程清理完成");
        } catch (Exception e) {
            log.warn("【LibreOffice服务】清理残留进程时出现异常（可忽略）: {}", e.getMessage());
        }
    }

    /**
     * 使用 LibreOffice 服务模式将 DOCX 文件转换为 PDF（带重试机制）
     * @param docxFilePath DOCX源文件的绝对路径
     * @param outputDir    PDF输出目录的绝对路径
     * @return 转换后的PDF文件绝对路径，失败返回null
     */
    public static String convertDocxToPdfWithLibreOffice(String docxFilePath, String outputDir) {
        File docxFile = new File(docxFilePath);
        if (!docxFile.exists()) {
            log.error("源文件不存在: {}", docxFilePath);
            return null;
        }

        // 确保输出目录存在
        File outDirFile = new File(outputDir);
        if (!outDirFile.exists()) {
            outDirFile.mkdirs();
        }

        // 计算输出PDF文件路径
        String docxFileName = docxFile.getName();
        String pdfFileName = docxFileName.replaceAll("(?i)\\.docx?$", ".pdf");
        String pdfFilePath = outputDir + File.separator + pdfFileName;
        File pdfFile = new File(pdfFilePath);

        // 带重试的转换逻辑
        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            try {
                // 检查服务是否可用
                if (!serviceAvailable || documentConverter == null) {
                    log.warn("【LibreOffice服务】服务不可用，尝试重新启动...");
                    forceRebuildOfficeManager();
                    if (!serviceAvailable) {
                        log.error("【LibreOffice服务】服务重启失败，无法进行转换");
                        return null;
                    }
                }

                if (attempt > 0) {
                    log.info("【服务模式转换】第{}次重试: {}", attempt, docxFilePath);
                } else {
                    log.info("【服务模式转换】开始: {}", docxFilePath);
                }
                long startTime = System.currentTimeMillis();
                
                // 使用服务模式进行转换
                documentConverter.convert(docxFile).to(pdfFile).execute();
                
                long duration = System.currentTimeMillis() - startTime;
                
                if (pdfFile.exists()) {
                    log.info("【服务模式转换】成功: {} -> {} (耗时: {}ms)", docxFilePath, pdfFilePath, duration);
                    // 转换成功，重置失败计数器
                    consecutiveFailures.set(0);
                    return pdfFilePath;
                } else {
                    log.error("【服务模式转换】完成但PDF文件未生成: {}", pdfFilePath);
                }

            } catch (OfficeException e) {
                int failures = consecutiveFailures.incrementAndGet();
                log.error("【服务模式转换】异常（连续失败{}次）: {}", failures, e.getMessage(), e);

                // 连续失败达到阈值，强制重建服务
                if (failures >= FAILURE_THRESHOLD) {
                    log.warn("【LibreOffice服务】连续失败{}次，达到阈值，强制重建服务", failures);
                    forceRebuildOfficeManager();
                }

                // 如果还有重试机会，等待一段时间后重试
                if (attempt < MAX_RETRY) {
                    try {
                        long waitTime = 5000L * (attempt + 1); // 递增等待
                        log.info("【服务模式转换】等待{}ms后重试...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                    // 如果服务不可用，重建后重试
                    if (!serviceAvailable) {
                        forceRebuildOfficeManager();
                    }
                }
            }
        }

        log.error("【服务模式转换】所有重试均失败: {}", docxFilePath);
        return null;
    }

    /**
     * 检查LibreOffice是否已安装
     */
    public static boolean isLibreOfficeInstalled() {
        File officeHome = new File(LIBRE_OFFICE_HOME);
        return officeHome.exists() && officeHome.isDirectory();
    }

    /**
     * 检查服务是否可用
     */
    public static boolean isServiceAvailable() {
        return serviceAvailable && officeManager != null && officeManager.isRunning();
    }
}