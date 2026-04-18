package com.jsh.erp.aop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * API日志异步线程池配置
 * 
 * 用于异步发送API日志到Kafka，减少对接口性能的影响
 * 
 * 线程池参数说明：
 * - 核心线程数：5（始终保持存活的线程数）
 * - 最大线程数：20（线程池最大容量）
 * - 队列容量：1000（等待队列大小）
 * - 空闲线程存活时间：60秒（超过核心线程数的线程空闲后的存活时间）
 * - 拒绝策略：CallerRunsPolicy（队列满时由调用线程直接执行）
 * 
 * @author jshERP
 */
@Configuration
@EnableAsync
public class AsyncThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncThreadPoolConfig.class);

    /**
     * 核心线程数：始终保持存活的线程数
     */
    private static final int CORE_POOL_SIZE = 5;

    /**
     * 最大线程数：线程池能够容纳的最大线程数
     */
    private static final int MAX_POOL_SIZE = 20;

    /**
     * 任务队列容量：等待执行的任务队列大小
     */
    private static final int QUEUE_CAPACITY = 1000;

    /**
     * 空闲线程存活时间（秒）：超过核心线程数的线程空闲后的存活时间
     */
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * API日志异步线程池
     * 
     * 用于异步执行以下任务：
     * 1. 发送API日志到Kafka
     * 
     * 拒绝策略说明：
     * - 使用CallerRunsPolicy：当队列满时，由调用线程直接执行任务
     * - 这样可以保证日志不丢失，同时不会抛出异常影响主业务
     * 
     * @return 线程池执行器
     */
    @Bean("apiLogAsyncExecutor")
    public Executor apiLogAsyncExecutor() {
        logger.info("初始化API日志异步线程池");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设置核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        // 设置最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        // 设置任务队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        // 设置空闲线程存活时间
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        // 设置线程名前缀，便于日志追踪
        executor.setThreadNamePrefix("api-log-async-");

        // 设置拒绝策略：队列满时由调用线程直接执行
        // 这样可以保证日志不丢失，同时不会影响主业务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
