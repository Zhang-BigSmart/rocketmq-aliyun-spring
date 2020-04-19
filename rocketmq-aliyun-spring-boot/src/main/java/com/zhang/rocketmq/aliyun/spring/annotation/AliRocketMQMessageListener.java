package com.zhang.rocketmq.aliyun.spring.annotation;

import java.lang.annotation.*;

/**
 * @author : zzh
 * create at:  2020/4/16
 * @description:The annotation of Consumer
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AliRocketMQMessageListener {

    String NAME_SERVER_PLACEHOLDER = "${rocketmq.ali.name-server:}";
    String ACCESS_KEY_PLACEHOLDER = "${rocketmq.ali.access-key:}";
    String SECRET_KEY_PLACEHOLDER = "${rocketmq.ali.secret-key:}";

    /**
     * The property of "group-Id"
     * @return
     */
    String groupId();

    /**
     * The property of "name-server".
     */
    String nameServer() default NAME_SERVER_PLACEHOLDER;

    /**
     * The property of "access-key".
     */
    String accessKey() default ACCESS_KEY_PLACEHOLDER;

    /**
     * The property of "secret-key".
     */
    String secretKey() default SECRET_KEY_PLACEHOLDER;

    String topic();

    String expression() default "*";

    //String OnsChannel() default "ALIYUN";

    /**
     * 设置 Consumer 实例的消费模式，取值说明如下：
     *    CLUSTERING（默认值）：表示集群消费
     *    BROADCASTING：表示广播消费
     */
    MessageModel messageModel() default MessageModel.CLUSTERING;

    /**
     * 设置 Consumer 实例的消费线程数，默认值：20
     */
    int consumeThreadNums() default 20;

    /**
     * 设置消息消费失败的最大重试次数，默认值：16
     */
    int maxReconsumeTimes() default 16;

    /**
     * 设置每条消息消费的最大超时时间 单位：分钟。 默认值：15
     */
    long consumeTimeout() default 15L;

    /**
     * 只适用于顺序消息，设置消息消费失败的重试间隔时间
     */
    //long suspendTimeMillis();

    /**
     * 客户端本地的最大缓存消息数据，默认值：1000；单位：条
     */
    //int maxCachedMessageAmount();

    /**
     * 客户端本地的最大缓存消息大小，取值范围：16 MB ~ 2 GB；默认值：512 MB
     */
    //int maxCachedMessageSizeInMiB();
}
