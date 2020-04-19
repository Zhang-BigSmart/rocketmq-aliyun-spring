package com.zhang.rocketmq.aliyun.spring.support;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.zhang.rocketmq.aliyun.spring.annotation.AliRocketMQMessageListener;
import com.zhang.rocketmq.aliyun.spring.annotation.MessageModel;
import com.zhang.rocketmq.aliyun.spring.core.AliRocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Properties;

/**
 * @author : zzh
 * create at:  2020/4/16
 * @description:
 */
public class DefaultAliRocketMQListenerContainer implements InitializingBean, AliRocketMQListenerContainer, SmartLifecycle, ApplicationContextAware {
    private final static Logger log = LoggerFactory.getLogger(DefaultAliRocketMQListenerContainer.class);

    private ApplicationContext applicationContext;

    /**
     * The name of the DefaultRocketMQListenerContainer instance
     */
    private String name;

    private String nameServer;

    private String groupId;

    private String accessKey;

    private String secretKey;

    private String topic;

    private String expression;

    private MessageModel messageModel;

    private int consumeThreadNums;

    private int maxReconsumeTimes;

    private long consumeTimeout;

    private long suspendTimeMillis;

    private int maxCachedMessageAmount;

    private int maxCachedMessageSizeInMiB;

    private Consumer consumer;

    private boolean running;

    private AliRocketMQListener aliRocketMQListener;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameServer() {
        return nameServer;
    }

    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    private AliRocketMQMessageListener aliRocketMQMessageListener;

    public AliRocketMQMessageListener getAliRocketMQMessageListener() {
        return aliRocketMQMessageListener;
    }

    public AliRocketMQListener getAliRocketMQListener() {
        return aliRocketMQListener;
    }

    public void setAliRocketMQListener(AliRocketMQListener aliRocketMQListener) {
        this.aliRocketMQListener = aliRocketMQListener;
    }

    public void setAliRocketMQMessageListener(AliRocketMQMessageListener listener) {
        this.aliRocketMQMessageListener = listener;

        this.messageModel = listener.messageModel();
        this.consumeThreadNums = listener.consumeThreadNums();
        this.maxReconsumeTimes = listener.maxReconsumeTimes();
        this.consumeTimeout = listener.consumeTimeout();
        //this.suspendTimeMillis = listener.suspendTimeMillis();
        //this.maxCachedMessageAmount = listener.maxCachedMessageAmount();
        //this.maxCachedMessageSizeInMiB = listener.maxCachedMessageSizeInMiB();
    }

    @Override
    public void destroy() throws Exception {
        this.setRunning(false);
        if (Objects.nonNull(consumer)) {
            consumer.shutdown();
        }
        log.info("container destroyed, {}", this.toString());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initRocketMQPushConsumer();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() {
        if (this.isRunning()) {
            throw new IllegalStateException("container already running. " + this.toString());
        }
        try {
            consumer.start();
        } catch (ONSClientException e) {
            throw new IllegalStateException("Failed to start Ali RocketMQ push consumer", e);
        }
        this.setRunning(true);

        log.info("running container: {}", this.toString());
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            if (Objects.nonNull(consumer)) {
                consumer.shutdown();
            }
            setRunning(false);
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    private void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        // Returning Integer.MAX_VALUE only suggests that
        // we will be the first bean to shutdown and last bean to start
        return Integer.MAX_VALUE;
    }


    private void initRocketMQPushConsumer() throws Exception {
        log.info("initRocketMQPushConsumer - {}", name);
        if (aliRocketMQListener == null) {
            throw new IllegalArgumentException("Property 'rocketMQListener' is required");
        }
        Assert.notNull(groupId, "Property 'groupId' is required");
        Assert.notNull(nameServer, "Property 'nameServer' is required");
        Assert.notNull(topic, "Property 'topic' is required");

        Properties properties = new Properties();

        properties.put(PropertyKeyConst.GROUP_ID, groupId);
        properties.put(PropertyKeyConst.NAMESRV_ADDR, nameServer);
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        properties.put(PropertyKeyConst.ConsumeThreadNums, consumeThreadNums);
        properties.put(PropertyKeyConst.MaxReconsumeTimes, maxReconsumeTimes);
        properties.put(PropertyKeyConst.ConsumeTimeout, consumeTimeout);

        switch (messageModel) {
            case CLUSTERING:
                properties.put(PropertyKeyConst.MessageModel, MessageModel.CLUSTERING);
                break;
            case BROADCASTING:
                properties.put(PropertyKeyConst.MessageModel, MessageModel.BROADCASTING);
                break;
            default:
                throw new IllegalArgumentException("Property 'messageModel' was wrong.");
        }
        consumer = ONSFactory.createConsumer(properties);
        consumer.subscribe(topic, expression, aliRocketMQListener);
    }
}
