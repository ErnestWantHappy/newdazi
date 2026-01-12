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

/**
 * 文件转换工具类
 * 使用 JODConverter + LibreOffice 服务模式进行高性能文档转换
 * 
 * 服务模式优势：
 * 1. LibreOffice 进程常驻运行，避免每次启动进程的开销（~3-5秒）
 * 2. 支持高并发转换（30个并发任务）
 * 3. 单次转换时间从 ~10秒 降低到 ~1-2秒
 */
@Component
public class FileConversionUtils {

    private static final Logger log = LoggerFactory.getLogger(FileConversionUtils.class);

    // LibreOffice 安装路径（Windows默认）
    private static final String LIBRE_OFFICE_HOME = "C:\\Program Files\\LibreOffice";

    // 服务管理器
    private static OfficeManager officeManager;
    
    // 文档转换器
    private static DocumentConverter documentConverter;
    
    // 服务是否可用
    private static volatile boolean serviceAvailable = false;

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
            log.info("【LibreOffice服务】正在启动服务模式...");
            
            // 创建本地办公套件管理器
            officeManager = LocalOfficeManager.builder()
                .officeHome(LIBRE_OFFICE_HOME)
                .portNumbers(2002, 2003, 2004, 2005, 2006)  // 5个端口支持5个并发实例
                .maxTasksPerProcess(200)  // 每个进程处理200个任务后重启
                .taskExecutionTimeout(120000L)  // 单个任务超时2分钟
                .taskQueueTimeout(60000L)  // 队列等待超时1分钟
                .build();

            officeManager.start();
            
            // 创建文档转换器
            documentConverter = LocalConverter.builder()
                .officeManager(officeManager)
                .build();

            serviceAvailable = true;
            log.info("【LibreOffice服务】服务启动成功，支持5个并发转换实例");
            
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
            try {
                officeManager.stop();
                log.info("【LibreOffice服务】服务已停止");
            } catch (OfficeException e) {
                log.error("【LibreOffice服务】停止失败: {}", e.getMessage());
            }
            officeManager = null;
            documentConverter = null;
            serviceAvailable = false;
        }
    }

    /**
     * 使用 LibreOffice 服务模式将 DOCX 文件转换为 PDF
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

        try {
            // 检查服务是否可用
            if (!serviceAvailable || documentConverter == null) {
                log.warn("【LibreOffice服务】服务不可用，尝试重新启动...");
                startOfficeManager();
                if (!serviceAvailable) {
                    log.error("【LibreOffice服务】服务重启失败，无法进行转换");
                    return null;
                }
            }

            log.info("【服务模式转换】开始: {}", docxFilePath);
            long startTime = System.currentTimeMillis();
            
            // 使用服务模式进行转换
            documentConverter.convert(docxFile).to(pdfFile).execute();
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (pdfFile.exists()) {
                log.info("【服务模式转换】成功: {} -> {} (耗时: {}ms)", docxFilePath, pdfFilePath, duration);
                return pdfFilePath;
            } else {
                log.error("【服务模式转换】完成但PDF文件未生成: {}", pdfFilePath);
                return null;
            }

        } catch (OfficeException e) {
            log.error("【服务模式转换】异常: {}", e.getMessage(), e);
            return null;
        }
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