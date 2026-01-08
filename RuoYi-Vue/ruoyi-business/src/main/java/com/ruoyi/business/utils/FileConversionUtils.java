package com.ruoyi.business.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * 文件转换工具类
 * 使用 LibreOffice 将 DOCX 文件转换为 PDF，可完美保留表格、图片和样式
 */
public class FileConversionUtils {

    private static final Logger log = LoggerFactory.getLogger(FileConversionUtils.class);

    // LibreOffice 安装路径（Windows默认）
    private static final String LIBRE_OFFICE_PATH = "C:\\Program Files\\LibreOffice\\program\\soffice.exe";

    /**
     * 使用 LibreOffice 将 DOCX 文件转换为 PDF
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

        try {
            // 构建LibreOffice命令
            // soffice --headless --convert-to pdf --outdir "输出目录" "源文件"
            ProcessBuilder pb = new ProcessBuilder(
                LIBRE_OFFICE_PATH,
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputDir,
                docxFilePath
            );
            pb.redirectErrorStream(true);

            log.info("开始LibreOffice转换: {}", docxFilePath);
            Process process = pb.start();

            // 读取命令输出（用于调试）
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待转换完成，最多60秒
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("LibreOffice转换超时");
                return null;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("LibreOffice转换失败，退出码: {}, 输出: {}", exitCode, output.toString());
                return null;
            }

            // 计算生成的PDF文件名（与源文件同名，扩展名改为.pdf）
            String docxFileName = docxFile.getName();
            String pdfFileName = docxFileName.replaceAll("(?i)\\.docx?$", ".pdf");
            String pdfFilePath = outputDir + File.separator + pdfFileName;

            File pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                log.info("LibreOffice转换成功: {} -> {}", docxFilePath, pdfFilePath);
                return pdfFilePath;
            } else {
                log.error("LibreOffice转换完成但PDF文件未生成: {}", pdfFilePath);
                return null;
            }

        } catch (Exception e) {
            log.error("LibreOffice转换异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查LibreOffice是否已安装
     */
    public static boolean isLibreOfficeInstalled() {
        File soffice = new File(LIBRE_OFFICE_PATH);
        return soffice.exists() && soffice.canExecute();
    }
}