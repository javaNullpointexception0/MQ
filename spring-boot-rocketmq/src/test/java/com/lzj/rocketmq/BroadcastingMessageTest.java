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
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 广播模式：
 * 发送端：正常发送
 * 消费端：设置广播消费模式
 */
@SpringBootTest
@Slf4j
public class BroadcastingMessageTest {

    @Test
    public void sendMessage() throws Exception {
        DefaultMQProducer defaultMQProducer = RocketMqUtil.getDefaultMQProducer();
        Message message = new Message(RocketMqUtil.TOPIC, "broadcasting",
                "broadcasting-message".getBytes(Charset.forName("UTF-8")));
        //按正常操作发送消息
        SendResult sendResult = defaultMQProducer.send(message);
        log.info("发送消息结果：{}", sendResult.getSendStatus().name());
    }

    @Test
    public void consumer() throws Exception {
        DefaultMQPushConsumer defaultMQPushConsumer = RocketMqUtil.getDefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        defaultMQPushConsumer.subscribe(RocketMqUtil.TOPIC, "*");
        //设置消费消息为广播模式
        defaultMQPushConsumer.setMessageModel(MessageModel.BROADCASTING);
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                msgs.stream().map(MessageExt::getBody).map(String::new).forEach(body -> log.info("消息内容：{}", body));
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        defaultMQPushConsumer.start();
        Thread.sleep(5000L);
        defaultMQPushConsumer.shutdown();
    }
}
