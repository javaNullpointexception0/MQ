package com.lzj.rocketmq;

import com.lzj.rocketmq.utils.RocketMqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 延迟消息主要通过对Message设置延迟级别实现，正产者和消费者按照正常逻辑进行生产和消费
 * 延时消息不是延迟发送，消息是实时发送的，只是消费者延迟消费
 */
@SpringBootTest
@Slf4j
public class ScheduleMessageTest {

    @Test
    public void sendMessage() throws Exception {
        DefaultMQProducer defaultMQProducer = RocketMqUtil.getDefaultMQProducer();
        Message message = new Message(RocketMqUtil.TOPIC, "schedule",
                "schedule-message".getBytes(Charset.forName("UTF-8")));
        //设置延迟级别，延迟级别≠延迟时间
        message.setDelayTimeLevel(4);
        SendResult sendResult = defaultMQProducer.send(message);
        log.info("发送消息结果：{}", sendResult.getSendStatus().name());
    }

    @Test
    public void consumer() throws Exception {
        DefaultMQPushConsumer defaultMQPushConsumer = RocketMqUtil.getDefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        defaultMQPushConsumer.subscribe(RocketMqUtil.TOPIC, "schedule");
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt messageExt : msgs) {
                    log.info("消息延时时间：{}毫秒，消息内容：{}",
                            System.currentTimeMillis() - messageExt.getBornTimestamp(),
                            new String(messageExt.getBody(), Charset.forName("UTF-8")));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        defaultMQPushConsumer.start();
        Thread.sleep(30000L);
        defaultMQPushConsumer.shutdown();
    }
}
