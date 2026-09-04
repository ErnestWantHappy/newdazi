package com.ruoyi.business.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 * 用于文件转换等耗时异步任务
 *
 * 拆成两级线程池，避免交卷 HTTP 线程被预览转换拖死：
 * 1. conversionDispatchExecutor：领取 pending→converting、投递真正转换任务（轻量 DB）
 * 2. conversionExecutor：实际 LibreOffice 转换，线程数 = 实例数
 *
 * 注意：选择题/判断题/打字题的提交走普通 HTTP 线程池，不受 conversionExecutor 影响。
 * 交卷成功与预览 PDF 生成解耦——交卷只保证答案落库，预览可排队稍后完成。
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Value("${ruoyi.libre-office.instance-count:5}")
    private int officeInstanceCount;

    @Value("${ruoyi.conversion.queue-capacity:800}")
    private int conversionQueueCapacity;

    @Value("${ruoyi.conversion.dispatch-core-size:10}")
    private int dispatchCoreSize;

    @Value("${ruoyi.conversion.dispatch-max-size:20}")
    private int dispatchMaxSize;

    @Value("${ruoyi.conversion.dispatch-queue-capacity:2000}")
    private int dispatchQueueCapacity;

    /**
     * 转换领取/投递线程池：只做短 SQL 与任务入队，不跑 LibreOffice。
     * 交卷 afterCommit 只往这里丢任务，保证 HTTP 尽快返回。
     */
    @Bean("conversionDispatchExecutor")
    public ThreadPoolTaskExecutor conversionDispatchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int core = Math.max(dispatchCoreSize, 4);
        int max = Math.max(dispatchMaxSize, core);
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(Math.max(dispatchQueueCapacity, 500));
        executor.setThreadNamePrefix("conv-dispatch-");
        // 领取本身很快；队列满时由调用方线程执行一次领取，避免静默丢任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 文件转换专用线程池（仅用于操作题 docx→pdf 转换）
     * 与 LibreOffice 实例数保持一致，避免实际并发超过服务池容量。
     */
    @Bean("conversionExecutor")
    public ThreadPoolTaskExecutor conversionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int poolSize = Math.max(officeInstanceCount, 1);
        // 核心线程数与 LibreOffice 实例数保持一致。
        executor.setCorePoolSize(poolSize);
        // 最大线程数与核心线程数保持一致，避免瞬时并发超过 LibreOffice 实例数。
        executor.setMaxPoolSize(poolSize);
        // 队列容量：全县多班级同时提交时排队等待
        executor.setQueueCapacity(Math.max(conversionQueueCapacity, 500));
        // 线程名前缀
        executor.setThreadNamePrefix("conversion-");
        // 禁止 CallerRuns：队列满时绝不能在 Tomcat/领取线程上跑 LibreOffice（会占满连接与请求）
        // 拒绝后由定时重试/教师重转把 converting/pending 任务捞回
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // 等待所有任务完成后再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间（秒），给足时间让队列中的转换任务完成
        executor.setAwaitTerminationSeconds(300);
        executor.initialize();
        return executor;
    }

    /** AI 外部调用独立排队，避免占用文件转换与 Tomcat 请求线程。 */
    @Bean("practicalAiExecutor")
    public ThreadPoolTaskExecutor practicalAiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("practical-ai-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /** 判题网络轮询独立隔离，Judge0 停顿时不挤占文件转换或外部 AI 线程。 */
    @Value("${ruoyi.judge.core-pool-size:2}")
    private int judgeCorePoolSize;

    @Value("${ruoyi.judge.max-pool-size:4}")
    private int judgeMaxPoolSize;

    @Value("${ruoyi.judge.queue-capacity:500}")
    private int judgeQueueCapacity;

    @Bean("judge0Executor")
    public ThreadPoolTaskExecutor judge0Executor() {
        if (judgeCorePoolSize <= 0 || judgeMaxPoolSize < judgeCorePoolSize || judgeQueueCapacity <= 0) {
            throw new IllegalStateException("判题线程池配置无效：核心线程、最大线程和队列容量必须为正数，且最大线程不能小于核心线程");
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(judgeCorePoolSize);
        executor.setMaxPoolSize(judgeMaxPoolSize);
        // ThreadPoolExecutor 会先使用核心线程，再进入队列；生产环境把核心线程与 Judge0 worker 数对齐，
        // 才能避免“队列已有数百任务但仍只有两个线程判题”的假扩容。
        executor.setQueueCapacity(judgeQueueCapacity);
        executor.setThreadNamePrefix("judge0-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
