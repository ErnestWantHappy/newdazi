package com.ruoyi.business.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.mapper.PythonPracticeMapper;

/** 应用重启后收口独立刷题的遗留判题记录，避免永久停留在等待或判题中。 */
@Component
public class PythonPracticeRecoveryService {
    @Autowired private PythonPracticeMapper mapper;
    @Autowired private PythonPracticeService service;
    @Autowired private Judge0Properties properties;
    @Value("${ruoyi.migration.startup-recovery-enabled:true}")
    private boolean startupRecoveryEnabled = true;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverStuckSubmissions() {
        if (!startupRecoveryEnabled) return;
        long seconds = Math.max(30, properties.getRecoveryTimeoutSeconds());
        Date before = new Date(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(seconds));
        List<Map<String, Object>> rows = mapper.selectStuckSubmissions(before, 200);
        for (Map<String, Object> row : rows) {
            Object id = row.get("submission_id");
            if (id != null) service.markStuckSubmissionAsServiceFailure(Long.valueOf(String.valueOf(id)));
        }
    }
}
