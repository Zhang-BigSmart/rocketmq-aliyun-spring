package com.zhang.rocketmq.aliyun.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rocketmq.ali")
public class AliRocketMQProperties {

    /**
     * The name server for rocketMQ, formats: `host:port;host:port`.
     */
    private String nameServer;

    private String accessKey;

    private String secretKey;

    private Producer producer;

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

    public String getNameServer() {
        return nameServer;
    }

    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }

    public AliRocketMQProperties.Producer getProducer() {
        return producer;
    }

    public void setProducer(AliRocketMQProperties.Producer producer) {
        this.producer = producer;
    }

    public static class Producer {

        /**
         * Group name of producer.
         */
        private String groupId;

        /**
         * The property of "accessKey".
         */
        private String accessKey;

        /**
         * The property of "secretKey".
         */
        private String secretKey;

        /**
         * 是否开启顺序消息
         */
        private boolean orderEnable;

        /**
         * 是否开启事物消息
         */
        private boolean transactionEnable;

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

        public boolean isOrderEnable() {
            return orderEnable;
        }

        public void setOrderEnable(boolean orderEnable) {
            this.orderEnable = orderEnable;
        }

        public boolean isTransactionEnable() {
            return transactionEnable;
        }

        public void setTransactionEnable(boolean transactionEnable) {
            this.transactionEnable = transactionEnable;
        }
    }

}
