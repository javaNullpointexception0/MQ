package com.lzj.rocketmq;

import com.lzj.rocketmq.config.RocketMqConfig;
import com.lzj.rocketmq.utils.RocketMqUtil;
import com.lzj.rocketmq.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Created by luzhenjiang
 * @date 2021/10/28 19:17
 * @description 简单消息测试
 */
@SpringBootTest
@Slf4j
public class SimpleMessageTest {

    @Test
    public void test(){
        int flag = 2;
        switch (flag) {
            case 0:
                System.out.println(0);
            case 1:
                System.out.println(1);
            case 2:
                System.out.println(2);
                default:
                    System.out.println(-1);
                    break;
        }
    }

    /**
     * 发送同步消息
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     * @throws MQBrokerException
     */
    @Test
    public void sendMessageSynchronously() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {

        DefaultMQProducer defaultMQProducer = RocketMqUtil.getDefaultMQProducer();
        Message message = new Message(RocketMqUtil.TOPIC, "send_message_synchronously", "同步消息".getBytes(Charset.forName("UTF-8")));
        SendResult sendResult = defaultMQProducer.send(message);
        log.info("同步消息，结果名称：{}，结果值：{}", sendResult.getSendStatus().name(),
                sendResult.getSendStatus().ordinal());
        defaultMQProducer.shutdown();
    }

    /**
     * 发送异步消息
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     */
    @Test
    public void sendMessageAsynchronously() throws RemotingException, MQClientException, InterruptedException {
        DefaultMQProducer defaultMQProducer = RocketMqUtil.getDefaultMQProducer();
        Message message = new Message(RocketMqUtil.TOPIC, "send_message_asynchronously", "异步消息".getBytes(Charset.forName("UTF-8")));
        defaultMQProducer.send(message, new SendCallback(){

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("send message success." + sendResult.getSendStatus().ordinal());
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("send message fail.");
            }
        });
        Thread.sleep(5000L);
        defaultMQProducer.shutdown();
    }

    /**
     * 单向传输，客户端只管发送，不等待响应，存在一定的不安全性，一般使用于记录日志的功能
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     */
    @Test
    public void sendMessageOneway() throws RemotingException, MQClientException, InterruptedException {
        DefaultMQProducer defaultMQProducer = RocketMqUtil.getDefaultMQProducer();
        Message message = new Message(RocketMqUtil.TOPIC, "send_message_oneway", "单向消息".getBytes(Charset.forName("UTF-8")));
        defaultMQProducer.sendOneway(message);
        defaultMQProducer.shutdown();
    }

    @Test
    public void consumeMessage() throws Exception {
        DefaultMQPushConsumer defaultMQPushConsumer = RocketMqUtil.getDefaultMQPushConsumer();
        defaultMQPushConsumer.subscribe(RocketMqUtil.TOPIC, "*");
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list,
                                                            ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                log.info("消费到消息条数：{}", list.size());
                list.stream().map(messageExt -> new String(messageExt.getBody(), Charset.forName("UTF-8")))
                        .map(String::new).forEach(System.out::println);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        defaultMQPushConsumer.start();
        Thread.sleep(20000L);
    }

    @Test
    public void consumeMessage2() throws Exception {
        DefaultLitePullConsumer defaultLitePullConsumer = new DefaultLitePullConsumer("TEST_CONSUMER");
        defaultLitePullConsumer.setNamesrvAddr(SpringUtil.getBean(RocketMqConfig.class).getNamesrvAddr());
        defaultLitePullConsumer.subscribe(RocketMqUtil.TOPIC, "send_message_synchronously");
        defaultLitePullConsumer.setAutoCommit(true);
        defaultLitePullConsumer.start();
        List<MessageExt> list = defaultLitePullConsumer.poll();
        log.info("消费到消息条数：{}", list.size());
        Thread.sleep(5000L);
    }


}
