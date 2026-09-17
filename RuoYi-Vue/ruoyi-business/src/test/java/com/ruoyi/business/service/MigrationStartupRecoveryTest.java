package com.ruoyi.business.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 迁移隔离必须在任何数据库或外部服务访问之前生效。 */
class MigrationStartupRecoveryTest
{
    @Test
    void disabledRecoveryDoesNotAccessDependencies()
    {
        PracticalAiJobRecoveryService ai = new PracticalAiJobRecoveryService();
        ProgrammingSubmissionRecoveryService programming = new ProgrammingSubmissionRecoveryService();
        PracticalArtifactService artifacts = new PracticalArtifactService();
        ReflectionTestUtils.setField(ai, "startupRecoveryEnabled", false);
        ReflectionTestUtils.setField(programming, "startupRecoveryEnabled", false);
        ReflectionTestUtils.setField(artifacts, "startupRecoveryEnabled", false);
        ai.recoverIncompleteJobs();
        programming.recoverStuckSubmissions();
        artifacts.recoverOfficeAttachmentsMissingPreviewAfterStartup();
    }
}
