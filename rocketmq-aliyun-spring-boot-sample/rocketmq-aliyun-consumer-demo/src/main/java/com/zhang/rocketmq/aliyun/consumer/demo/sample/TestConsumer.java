package com.zhang.rocketmq.aliyun.consumer.demo.sample;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.zhang.rocketmq.aliyun.spring.annotation.AliRocketMQMessageListener;
import com.zhang.rocketmq.aliyun.spring.core.AliRocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author : zzh
 * create at:  2020/4/17
 * @description:
 */
@Component
@AliRocketMQMessageListener(
        accessKey = "${demo.consumer.test.accessKey}",
        secretKey = "${demo.consumer.test.secretKey}",
        groupId = "${demo.consumer.test.groupId}",
        topic = "${demo.consumer.test.topic}",
        expression = "${demo.consumer.test.tag}")
public class TestConsumer implements AliRocketMQListener {

    public Action consume(Message message, ConsumeContext context) {
        System.out.println("#### Receive Message ####");
        System.out.println("message id: " + message.getMsgID());
        System.out.println("topic: [" + message.getTopic() + "] - tag: [" + message.getTag() + "]");
        System.out.println("body: [" + new String(message.getBody()) + "]");
        System.out.println("#########################");
        return Action.CommitMessage;
    }
}
