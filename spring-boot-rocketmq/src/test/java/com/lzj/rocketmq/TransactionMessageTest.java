package com.lzj.rocketmq;

import com.lzj.rocketmq.config.RocketMqConfig;
import com.lzj.rocketmq.utils.RocketMqUtil;
import com.lzj.rocketmq.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.Random;

/**
 * 生产者添加事务监听器、指定check线程池、发送事务消息，消费者正常消费逻辑
 * 事务消息：
 * 1、不支持延时消息和批量消息
 * 2、如果消息没有及时提交，默认check 15次，可以通过Broker的transactionCheckMax参数配置次数。
 *    如果超时15次依然没有得到明确结果，将会打印，具体的处理策略可以通过复写AbstractTransactionCheckListener类实现
 * 3、每次check的时间间隔可以通过Broker的transactionTimeout配置，也可以在消息中增加CHECK_IMMUNITY_TIME_IN_SECONDS属性指定
 * 4、事务状态：LocalTransactionState.COMMIT_MESSAGE、LocalTransactionState.ROLLBACK_MESSAGE、LocalTransactionState.UNKNOW
 */
@SpringBootTest
@Slf4j
public class TransactionMessageTest {

    @Test
    public void sendMessage() throws Exception {
        //事务生产者
        TransactionMQProducer producer = new TransactionMQProducer("defaultGroup");
        producer.setNamesrvAddr(SpringUtil.getBean(RocketMqConfig.class).getNamesrvAddr());
        //设置检查本地事务状态的线程池
        //producer.setExecutorService(null);
        //本地事务执行监听器
        TransactionListener transactionListener = new TransactionListenerImpl();
        producer.setTransactionListener(transactionListener);
        producer.start();
        Message message = new Message(RocketMqUtil.TOPIC, "transaction", "transaction-message".getBytes(Charset.forName("UTF-8")));
        //发送事务消息
        producer.sendMessageInTransaction(message, null);
    }

    class TransactionListenerImpl implements TransactionListener {

        @Override
        public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            //执行本地事务（数据库）操作......
            int num = new Random().nextInt(10);
            if (num < 3) {
                //本地事务执行成功，提交消息
                return LocalTransactionState.COMMIT_MESSAGE;
            } else if (num < 6) {
                //本地事务执行失败，删除消息
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
            //等待本地事务check，即执行checkLocalTransaction()方法
            return LocalTransactionState.UNKNOW;
        }

        /**
         * 回查逻辑
         * @param msg
         * @return
         */
        @Override
        public LocalTransactionState checkLocalTransaction(MessageExt msg) {
            int num = new Random().nextInt(10);
            if (num < 3) {
                //提交消息
                return LocalTransactionState.COMMIT_MESSAGE;
            } else if (num < 6) {
                //删除消息
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
            return LocalTransactionState.UNKNOW;
        }
    }
}
