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
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 文件转换专用线程池
     * 支持30个并发转换任务
     */
    @Bean("conversionExecutor")
    public Executor conversionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：30
        executor.setCorePoolSize(30);
        // 最大线程数：50（应对突发高峰）
        executor.setMaxPoolSize(50);
        // 队列容量：200（等待队列）
        executor.setQueueCapacity(200);
        // 线程名前缀
        executor.setThreadNamePrefix("conversion-");
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
