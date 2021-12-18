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
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 批量发送消息条件：
 * 1、发送到同一个topic
 * 2、等待同一个发送结果
 * 3、不允许使用定时消息
 * 4、同一批消息大小不能超过1MB，否则需要自己进行切割
 */
@SpringBootTest
@Slf4j
public class BatchMessageTest {

    @Test
    public void sendMessage() throws Exception {
        DefaultMQProducer defaultMQProducer = RocketMqUtil.getDefaultMQProducer();
        Message message1 = new Message(RocketMqUtil.TOPIC, "batch-1", "batch-message-1".getBytes(Charset.forName("UTF-8")));
        Message message2 = new Message(RocketMqUtil.TOPIC, "batch-2", "batch-message-2".getBytes(Charset.forName("UTF-8")));
        Message message3 = new Message(RocketMqUtil.TOPIC, "batch-3", "batch-message-3".getBytes(Charset.forName("UTF-8")));
        //批量发送消息
        List<Message> messages = Lists.newArrayList(message1, message2, message3);
        SendResult sendResult = defaultMQProducer.send(messages);
        log.info("发送消息结果：{}", sendResult.getSendStatus().name());
        defaultMQProducer.shutdown();
    }

    @Test
    public void consumer() throws Exception {
        DefaultMQPushConsumer defaultMQPushConsumer = RocketMqUtil.getDefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        defaultMQPushConsumer.subscribe(RocketMqUtil.TOPIC, "*");
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
