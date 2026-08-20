package com.ruoyi.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** 独立刷题判题异步执行器，避免学生请求线程等待 Judge0。 */
@Service
public class PythonPracticeSubmissionWorker {
    @Autowired private PythonPracticeService service;
    @Async("judge0Executor")
    public void judge(Long submissionId) { service.judgeSubmission(submissionId); }
}
