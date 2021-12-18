package com.lzj.rocketmq;

import com.lzj.rocketmq.utils.RocketMqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 消息过滤的两种方式：Tag和SQL表达式
 * 生产者：对Message设置用户属性
 * 消费者：subscribe时指定Tag、SQL表达式
 */
@SpringBootTest
@Slf4j
public class FilterMessageTest {

    @Test
    public void sendMessage() throws Exception {
        DefaultMQProducer defaultMQProducer = RocketMqUtil.getDefaultMQProducer();
        Message msg = new Message(RocketMqUtil.TOPIC, "filter", "filter-message-1".getBytes(Charset.forName("UTF-8")));
        //设置消息的属性
        msg.putUserProperty("age", "30");
        msg.putUserProperty("name", "lzj");
        SendResult sendResult = defaultMQProducer.send(msg);
        log.info("发送结果：{}", sendResult.getSendStatus().name());
        defaultMQProducer.shutdown();
    }

    @Test
    public void consumer() throws Exception {
        DefaultMQPushConsumer defaultMQPushConsumer = RocketMqUtil.getDefaultMQPushConsumer();
        //根据Tag过滤消息
        //defaultMQPushConsumer.subscribe(RocketMqUtil.TOPIC, "filter || Tag1");
        //根据Tag+SQL表达式过滤消息
        defaultMQPushConsumer.subscribe(RocketMqUtil.TOPIC, MessageSelector.byTag("filter || Tag1").bySql("age > 20 AND name = 'lzj'"));
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                msgs.stream().map(MessageExt::getBody).map(String::new).forEach(System.out::println);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        defaultMQPushConsumer.start();
        Thread.sleep(5000L);
        defaultMQPushConsumer.shutdown();
    }
}
