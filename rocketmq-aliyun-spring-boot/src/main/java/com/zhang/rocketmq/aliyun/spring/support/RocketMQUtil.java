package com.zhang.rocketmq.aliyun.spring.support;

import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import com.zhang.rocketmq.aliyun.spring.autoconfigure.AliRocketMQProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.Properties;

/**
 * @author : zzh
 * create at:  2020/4/15
 * @description:
 */
public class RocketMQUtil {
    private static final Logger log = LoggerFactory.getLogger(RocketMQUtil.class);

    /**
     * Check the validity of parameters in Message
     * @param message {@link com.aliyun.openservices.ons.api.Message}
     */
    public static void checkMsg(Message message) {
        if (Objects.isNull(message)) {
            log.error("syncSend failed. message is null");
            throw new IllegalArgumentException("`message` cannot be null");
        }
        if (StringUtils.isBlank(message.getTopic())) {
            log.error("syncSend failed. topic is null");
            throw new IllegalArgumentException("`message.topic` cannot be null");
        }
    }

    public static Properties creatMQProperties(AliRocketMQProperties aliRocketMQProperties, Environment environment) {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, aliRocketMQProperties.getNameServer());

        String accessKey = aliRocketMQProperties.getProducer().getAccessKey();
        accessKey = StringUtils.isBlank(accessKey) ? aliRocketMQProperties.getAccessKey() : accessKey;
        properties.setProperty(PropertyKeyConst.AccessKey, accessKey);

        String secretKey = aliRocketMQProperties.getProducer().getSecretKey();
        secretKey = StringUtils.isBlank(secretKey) ? aliRocketMQProperties.getSecretKey() : secretKey;
        properties.setProperty(PropertyKeyConst.SecretKey, secretKey);
        return properties;
    }
}
