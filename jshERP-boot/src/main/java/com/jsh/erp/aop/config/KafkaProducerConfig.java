package com.jsh.erp.aop.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka生产者配置
 * 
 * 用于配置Kafka生产者，将API日志发送到Kafka消息队列
 * 
 * 配置参数说明：
 * - bootstrap.servers：Kafka服务器地址列表
 * - acks：生产者确认模式（1表示Leader确认）
 * - retries：发送失败重试次数
 * - batch.size：批量发送大小
 * - linger.ms：发送延迟时间
 * - buffer.memory：发送缓冲区大小
 * 
 * @author jshERP
 */
@Configuration
@EnableKafka
public class KafkaProducerConfig {

    /**
     * Kafka服务器地址列表
     * 格式：host1:port1,host2:port2
     */
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * 生产者确认模式
     * 0：不等待确认
     * 1：等待Leader确认
     * all：等待所有ISR副本确认
     */
    @Value("${spring.kafka.producer.acks:1}")
    private String acks;

    /**
     * 发送失败重试次数
     */
    @Value("${spring.kafka.producer.retries:3}")
    private int retries;

    /**
     * 批量发送大小（字节）
     * 当多个消息要发送到同一个分区时，生产者会将它们打包成一个批次发送
     */
    @Value("${spring.kafka.producer.batch-size:16384}")
    private int batchSize;

    /**
     * 发送延迟时间（毫秒）
     * 生产者会等待指定时间，让更多消息加入批次后再发送
     */
    @Value("${spring.kafka.producer.linger-ms:1}")
    private int lingerMs;

    /**
     * 发送缓冲区大小（字节）
     * 生产者用来缓冲等待发送到服务器的消息的内存总量
     */
    @Value("${spring.kafka.producer.buffer-memory:33554432}")
    private int bufferMemory;

    /**
     * 创建Kafka生产者工厂
     * 
     * 配置生产者的核心参数：
     * 1. 服务器地址
     * 2. 确认模式
     * 3. 重试策略
     * 4. 批量发送配置
     * 5. 序列化器（使用StringSerializer）
     * 
     * @return 生产者工厂
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka服务器地址
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 生产者确认模式
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        // 发送失败重试次数
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        // 批量发送大小
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        // 发送延迟时间
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        // 发送缓冲区大小
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        // Key序列化器
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Value序列化器
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * 创建KafkaTemplate
     * 
     * KafkaTemplate是Spring提供的Kafka操作模板，封装了Kafka生产者的常用操作
     * 用于发送消息到Kafka主题
     * 
     * @return KafkaTemplate实例
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
