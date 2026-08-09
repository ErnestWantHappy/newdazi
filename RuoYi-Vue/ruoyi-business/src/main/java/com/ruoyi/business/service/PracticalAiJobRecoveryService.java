package com.ruoyi.business.service;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.PracticalAiEvent;
import com.ruoyi.business.domain.PracticalAiJob;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;

/** 应用重启后只接续未完成结果，成功或失败结果不会再次调用模型。 */
@Service
public class PracticalAiJobRecoveryService
{
    @Autowired private PracticalAiGradingMapper mapper;
    @Autowired private PracticalAiJobWorker worker;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverIncompleteJobs()
    {
        for (PracticalAiJob job : mapper.selectRecoverableJobs())
        {
            if ("CANCEL_REQUESTED".equals(job.getJobStatus()))
            {
                Date now = new Date();
                mapper.updatePendingResultsStatus(job.getJobId(), "CANCELLED", "教师已取消", now);
                mapper.updateJobCounts(job.getJobId());
                mapper.updateJobStatus(job.getJobId(), "CANCELLED", null, now, null);
                addEvent(job.getJobId(), "WARN", "CANCELLED", "服务启动时完成了此前等待中的取消请求");
                continue;
            }
            int interrupted = mapper.resetInterruptedResults(job.getJobId());
            mapper.updateJobStatus(job.getJobId(), "PENDING", null, null, null);
            mapper.updateJobHeartbeat(job.getJobId(), null);
            addEvent(job.getJobId(), interrupted > 0 ? "WARN" : "INFO", "AUTO_RECOVERED",
                    interrupted > 0 ? "服务已重启，正在从未完成作品安全接续" : "服务启动后自动接续排队任务");
            worker.run(job.getJobId());
        }
    }

    private void addEvent(Long jobId, String level, String stage, String message)
    {
        try
        {
            PracticalAiEvent event = new PracticalAiEvent();
            event.setJobId(jobId);
            event.setEventLevel(level);
            event.setEventStage(stage);
            event.setEventMessage(message);
            mapper.insertEvent(event);
        }
        catch (Exception ignored)
        {
            // 恢复任务优先于展示日志，日志失败不影响接续。
        }
    }
}
