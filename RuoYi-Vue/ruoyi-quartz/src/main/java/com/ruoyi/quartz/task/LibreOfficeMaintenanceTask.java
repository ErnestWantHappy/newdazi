package com.ruoyi.quartz.task;

import com.ruoyi.business.utils.FileConversionUtils;
import org.springframework.stereotype.Component;

/**
 * LibreOffice 转换服务维护任务。
 */
@Component("libreOfficeMaintenanceTask")
public class LibreOfficeMaintenanceTask {

    public String cleanupAndRestart() {
        return FileConversionUtils.cleanupAndRestartForMaintenance();
    }
}
