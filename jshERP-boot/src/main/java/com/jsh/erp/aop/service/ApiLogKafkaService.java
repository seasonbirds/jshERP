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

@Service
public class ApiLogKafkaService {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogKafkaService.class);

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${api.log.kafka.topic:api-log-topic}")
    private String kafkaTopic;

    @Value("${api.log.kafka.enabled:true}")
    private boolean kafkaEnabled;

    @Async("apiLogAsyncExecutor")
    public void sendApiLogToKafka(ApiLog apiLog) {
        if (!kafkaEnabled) {
            return;
        }

        if (kafkaTemplate == null) {
            logger.warn("KafkaTemplate未初始化，无法发送日志到Kafka");
            return;
        }

        try {
            String logJson = JSON.toJSONString(apiLog);
            kafkaTemplate.send(kafkaTopic, logJson);
            logger.debug("API日志已发送到Kafka，topic: {}, log: {}", kafkaTopic, apiLog.getUrl());
        } catch (Exception e) {
            logger.error("发送API日志到Kafka失败: {}", e.getMessage(), e);
        }
    }
}
