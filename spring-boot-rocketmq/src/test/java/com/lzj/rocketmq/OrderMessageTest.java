package com.lzj.rocketmq;

import com.lzj.rocketmq.utils.RocketMqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.List;

@SpringBootTest
@Slf4j
public class OrderMessageTest {

    @Test
    public void sendMessageOrderly() throws Exception {
        DefaultMQProducer defaultMQProducer = RocketMqUtil.getDefaultMQProducer();
        int arg = 3;
        for (int i = 0; i < 3; i++) {
            Message message = new Message(RocketMqUtil.TOPIC, "orderly", "顺序消息".getBytes(Charset.forName("UTF-8")));
            //为了保证消息顺序，则消息发送到同一个队列中，可通过MessageQueueSelector实现
            // 可以通过arg参数在内部协助计算发送到哪个队列
            defaultMQProducer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    //根据参数选定消息发送到哪个队列，确保同类消息在同一队列中，以确保消息是按顺序存放
                    int index = ((int) arg) % mqs.size();
                    log.info("队列数：{}，当前队列数：{}", mqs.size(), index);
                    return mqs.get(index);
                }
            }, arg);
        }
        defaultMQProducer.shutdown();
    }

    @Test
    public void consumeMessageOrderly () throws Exception {
        DefaultMQPushConsumer defaultMQPushConsumer = RocketMqUtil.getDefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        defaultMQPushConsumer.subscribe(RocketMqUtil.TOPIC, "*");
        //消费监听器指定顺序消息监听器
        defaultMQPushConsumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs,
                                                       ConsumeOrderlyContext context) {
                log.info("消费到消息条数：{}", msgs.size());
                msgs.stream().map(messageExt -> new String(messageExt.getBody(), Charset.forName("UTF-8")))
                        .map(String::new).forEach(System.out::println);
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        defaultMQPushConsumer.start();
        Thread.sleep(5000L);
        defaultMQPushConsumer.shutdown();
    }
}
