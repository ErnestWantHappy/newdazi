package com.ruoyi.business.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.ruoyi.business.domain.ProgrammingSubmission;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;

/** 仅在平台启动完成后处理遗留任务，避免异常重启后学生一直看到“判题中”。 */
@Component
public class ProgrammingSubmissionRecoveryService {
    @Autowired private ProgrammingJudgeMapper programmingMapper;
    @Autowired private ProgrammingSubmissionService programmingSubmissionService;
    @Autowired private Judge0Properties properties;
    @Value("${ruoyi.migration.startup-recovery-enabled:true}")
    private boolean startupRecoveryEnabled = true;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverStuckSubmissions() {
        // 迁入的进行中记录需要先对账，不能在验证启动时改成失败。
        if (!startupRecoveryEnabled) return;
        long seconds = Math.max(30, properties.getRecoveryTimeoutSeconds());
        Date before = new Date(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(seconds));
        List<ProgrammingSubmission> submissions = programmingMapper.selectStuckSubmissions(before, 200);
        for (ProgrammingSubmission submission : submissions) {
            programmingSubmissionService.markStuckSubmissionAsServiceFailure(submission.getSubmissionId());
        }
    }
}
