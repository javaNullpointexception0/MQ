package com.lzj.rocketmq.utils;

import com.lzj.rocketmq.config.RocketMqConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

public class RocketMqUtil {

    public static final String TOPIC = "topic_simple_message";

    public static DefaultMQProducer getDefaultMQProducer() {
        try {
            DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
            defaultMQProducer.setProducerGroup("TEST_PRODUCER");
            defaultMQProducer.setNamesrvAddr(SpringUtil.getBean(RocketMqConfig.class).getNamesrvAddr());
            //同步发送消息时发送失败后的重试次数
            defaultMQProducer.setRetryTimesWhenSendFailed(0);
            //异步发送消息时发送失败后的重试次数
            defaultMQProducer.setRetryTimesWhenSendAsyncFailed(0);
            defaultMQProducer.start();
            return defaultMQProducer;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化RocketMQ生产者时发生异常");
        }
    }

    public static DefaultMQPushConsumer getDefaultMQPushConsumer() throws MQClientException {
        try {
            DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
            defaultMQPushConsumer.setConsumerGroup("TEST_CONSUMER");
            defaultMQPushConsumer.setNamesrvAddr(SpringUtil.getBean(RocketMqConfig.class).getNamesrvAddr());
            return defaultMQPushConsumer;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化RocketMQ消费者时发生异常");
        }
    }
}
