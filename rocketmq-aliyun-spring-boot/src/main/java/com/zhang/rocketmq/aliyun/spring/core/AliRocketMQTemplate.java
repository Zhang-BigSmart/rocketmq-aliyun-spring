package com.zhang.rocketmq.aliyun.spring.core;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.zhang.rocketmq.aliyun.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

/**
 * @author : zzh
 * create at:  2020/4/15
 * @description: 普通消息Template
 */
public class AliRocketMQTemplate implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(AliRocketMQTemplate.class);

    private Producer producer;

    private OrderProducer orderProducer;


    /**
     * Send message in synchronous mode. This method returns only when the sending procedure totally completes.
     * Reliable synchronous transmission is used in extensive scenes, such as important notification messages, SMS
     * notification, SMS marketing system, etc..
     *
     * @param message {@link com.aliyun.openservices.ons.api.Message}
     * @return {@link SendResult}
     */
    public SendResult sendSync(Message message) {
        RocketMQUtil.checkMsg(message);
        try {
            long now = System.currentTimeMillis();
            SendResult sendResult = producer.send(message);
            long costTime = System.currentTimeMillis() - now;
            if (log.isDebugEnabled()) {
                log.debug("send message cost: {} ms, msgId:{}", costTime, sendResult.getMessageId());
            }
            return sendResult;
        } catch (Exception e) {
            log.error("sendSync failed. topic:{}, message:{} ", message.getTopic(), message);
            throw new ONSClientException(e.getMessage(), e);
        }
    }

    /**
     * Send orderly message. shardingKey will be distinguished the global order and  relative order.
     * @param message {@link com.aliyun.openservices.ons.api.Message}
     * @param shardingKey
     * @return
     */
    public SendResult sendOrderly(Message message, String shardingKey) {
        RocketMQUtil.checkMsg(message);
        if (Objects.isNull(orderProducer)) {
            log.error("sendOrderly failed. order-enable must be true");
            throw new IllegalArgumentException("`order-enable` must be true");
        }
        try {
            return orderProducer.send(message, shardingKey);
        } catch (Exception e) {
            log.error("sendSyncOrderly failed. topic:{}, message:{} ", message.getTopic(), message);
            throw new ONSClientException(e.getMessage(), e);
        }
    }

    /**
     * Send message in asynchronous mode. Asynchronous transmission is generally used in response time
     * sensitive business scenarios.
     *
     * This method returns immediately. On sending completion, <code>sendCallback</code> will be executed.
     * @param message
     * @param sendCallback
     */
    public void sendAsync(Message message, SendCallback sendCallback) {
        RocketMQUtil.checkMsg(message);
        try {
            producer.sendAsync(message, sendCallback);
        } catch (Exception e){
            log.error("sendAsync failed. topic:{}, message:{} ", message.getTopic(), message);
            throw new ONSClientException(e.getMessage(), e);
        }
    }

    /**
     * This method won't wait for acknowledge from broker before return. Obviously, it has maximums throughput yet potentials of message loss.
     *
     * One-way transmission is used for cases requiring moderate reliability, such as log collection.
     * @param message {@link com.aliyun.openservices.ons.api.Message}
     */
    public void sendOneWay(Message message) {
        RocketMQUtil.checkMsg(message);
        try {
            producer.sendOneway(message);
        } catch (Exception e) {
            log.error("sendOneWay failed. topic:{}, message:{} ", message.getTopic(), message);
            throw new ONSClientException(e.getMessage(), e);
        }
    }


    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(producer)) {
            producer.shutdown();
        }
        if (Objects.nonNull(orderProducer)) {
            orderProducer.shutdown();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Objects.nonNull(producer)) {
            producer.start();
        }
        if (Objects.nonNull(orderProducer)) {
            orderProducer.start();
        }
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public OrderProducer getOrderProducer() {
        return orderProducer;
    }

    public void setOrderProducer(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }
}
