package com.ruoyi.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** HTTP 请求只落库和投递，避免学生请求线程被 Judge0 长轮询占满。 */
@Service
public class ProgrammingSubmissionWorker {
    @Autowired private ProgrammingSubmissionService programmingSubmissionService;
    @Async("judge0Executor")
    public void judge(Long submissionId) { programmingSubmissionService.judgeSubmission(submissionId); }
}
