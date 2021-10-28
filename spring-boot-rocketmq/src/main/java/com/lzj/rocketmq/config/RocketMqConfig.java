package com.lzj.rocketmq.config;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by luzhenjiang
 * @date 2021/10/28 14:03
 * @description
 */
@Configuration
public class RocketMqConfig {

    @Value("${rocketmq.namesrv.addr}")
    private String namesrvAddr;

    @Bean
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
        defaultMQProducer.setNamesrvAddr(namesrvAddr);
        defaultMQProducer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            defaultMQProducer.shutdown();
        }));

        return defaultMQProducer;
    }

    
}
