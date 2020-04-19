package com.zhang.rocketmq.aliyun.produce.demo;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.zhang.rocketmq.aliyun.spring.core.AliRocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProduceApplication implements CommandLineRunner {

    @Autowired
    private AliRocketMQTemplate rocketMQTemplate;

    public static void main(String[] args) {
        SpringApplication.run(ProduceApplication.class, args);
    }

    public void run(String... args) throws Exception {
//        sendSync();
//        sendAsync();
//        sendOneWay();
        sendOrder();
    }

    /**
     * 普通同步消息
     */
    public void sendSync() {
        Message message = new Message();
        message.setBody("hello world!!!".getBytes());
        message.setTopic("test");
        SendResult result = rocketMQTemplate.sendSync(message);
        System.out.println(result.getMessageId());
    }

    /**
     * 普通异步消息
     */
    public void sendAsync() {
        Message message = new Message();
        message.setBody("hello world!!!".getBytes());
        message.setTopic("test");
        rocketMQTemplate.sendAsync(message, new SendCallback(){
            public void onSuccess(SendResult sendResult) {
                System.out.println("send success");
            }

            public void onException(OnExceptionContext context) {
                System.out.println("send exception");
            }
        });
    }

    /**
     * 普通单向消息
     */
    public void sendOneWay() {
        Message message = new Message();
        message.setBody("sendOneWay".getBytes());
        message.setTopic("test");
        rocketMQTemplate.sendOneWay(message);
    }

    public void sendOrder() {
        Message message = new Message();
        message.setBody("sendOrder".getBytes());
//        message.setTopic("test_order_relative");
        message.setTopic("test");
        message.setKey("1");
        String shardingKey = String.valueOf(1);
        SendResult result = null;
        try {
            result = rocketMQTemplate.sendOrderly(message, shardingKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result.getMessageId());
    }
}
