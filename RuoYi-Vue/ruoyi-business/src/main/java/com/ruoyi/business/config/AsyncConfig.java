package com.ruoyi.business.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 * 用于文件转换等耗时异步任务
 * 
 * 重要：线程池大小必须与 LibreOffice 实例数匹配
 * - 核心线程数 = LibreOffice 实例数（5个）
 * - 最大线程数 = LibreOffice 实例数（5个），严格限制实际并发
 * - 队列容量 200，全县级平台高并发时排队等待而非拒绝
 * 
 * 注意：此线程池仅服务于操作题 Word→PDF 转换，
 * 选择题/判断题/打字题的提交走普通 HTTP 线程池，不受此配置影响。
 * 
 * 设计原理：
 * 1. 线程数 ≈ LibreOffice 实例数，避免大量线程争抢少量实例导致超时
 * 2. 多出的并发任务进入队列有序排队，LibreOffice 空闲时自动消费
 * 3. CallerRunsPolicy 作为兜底，队列满时由调用线程执行（背压机制）
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 文件转换专用线程池（仅用于操作题 docx→pdf 转换）
     * 与 FileConversionUtils 中的 OFFICE_INSTANCE_COUNT(5) 保持一致
     */
    @Bean("conversionExecutor")
    public Executor conversionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数 = LibreOffice 实例数（5个）
        executor.setCorePoolSize(5);
        // 最大线程数与核心线程数保持一致，避免瞬时并发超过 LibreOffice 实例数
        executor.setMaxPoolSize(5);
        // 队列容量：全县多班级同时提交时排队等待
        executor.setQueueCapacity(200);
        // 线程名前缀
        executor.setThreadNamePrefix("conversion-");
        // 拒绝策略：由调用线程执行（兜底背压）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间（秒），给足时间让队列中的转换任务完成
        executor.setAwaitTerminationSeconds(300);
        executor.initialize();
        return executor;
    }
}
