package com.jsh.erp.aop.service;

import com.alibaba.fastjson.JSON;
import com.jsh.erp.aop.entity.ApiLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * API日志Kafka发送服务
 * 
 * 负责将API日志异步发送到Kafka消息队列
 * 
 * 特点：
 * 1. 使用@Async注解异步执行，不阻塞主业务线程
 * 2. 使用自定义线程池apiLogAsyncExecutor
 * 3. 所有异常都被捕获，不影响主业务
 * 4. 支持通过配置开关控制是否发送到Kafka
 * 
 * @author jshERP
 */
@Service
public class ApiLogKafkaService {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogKafkaService.class);

    /**
     * Kafka模板（可选注入，如果Kafka未配置则为null）
     */
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Kafka主题名称
     * 用于存储API日志的主题
     */
    @Value("${api.log.kafka.topic:api-log-topic}")
    private String kafkaTopic;

    /**
     * 是否启用Kafka日志发送
     * true：发送日志到Kafka
     * false：不发送到Kafka（只打印到控制台）
     */
    @Value("${api.log.kafka.enabled:true}")
    private boolean kafkaEnabled;

    /**
     * 异步发送API日志到Kafka
     * 
     * 使用@Async注解标记为异步方法，将使用apiLogAsyncExecutor线程池执行
     * 
     * 处理流程：
     * 1. 检查是否启用Kafka发送，未启用则直接返回
     * 2. 检查KafkaTemplate是否已初始化，未初始化则记录警告
     * 3. 将ApiLog对象转为JSON字符串
     * 4. 发送到Kafka主题
     * 5. 捕获所有异常，记录日志但不抛出
     * 
     * 注意：
     * - 此方法异步执行，不会阻塞调用线程
     * - 所有异常都被捕获，确保不影响主业务
     * 
     * @param apiLog API日志对象
     */
    @Async("apiLogAsyncExecutor")
    public void sendApiLogToKafka(ApiLog apiLog) {
        // 检查是否启用Kafka发送
        if (!kafkaEnabled) {
            return;
        }

        // 检查KafkaTemplate是否已初始化
        if (kafkaTemplate == null) {
            logger.warn("KafkaTemplate未初始化，无法发送日志到Kafka");
            return;
        }

        try {
            // 将日志对象转为JSON字符串
            String logJson = JSON.toJSONString(apiLog);
            // 发送到Kafka主题
            kafkaTemplate.send(kafkaTopic, logJson);
            logger.debug("API日志已发送到Kafka，topic: {}, log: {}", kafkaTopic, apiLog.getUrl());
        } catch (Exception e) {
            // 捕获所有异常，记录日志但不抛出
            // 确保Kafka发送失败不会影响主业务
            logger.error("发送API日志到Kafka失败: {}", e.getMessage(), e);
        }
    }
}
