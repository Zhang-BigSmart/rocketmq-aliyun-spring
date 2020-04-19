package com.zhang.rocketmq.aliyun.spring.autoconfigure;

import com.aliyun.openservices.ons.api.Admin;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.zhang.rocketmq.aliyun.spring.core.AliRocketMQTemplate;
import com.zhang.rocketmq.aliyun.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(AliRocketMQProperties.class)
@ConditionalOnClass({Admin.class})
@ConditionalOnProperty(prefix = "rocketmq.ali", value = "name-server", matchIfMissing = true)
@Import({ListenerContainerConfiguration.class})
public class AliRocketMQAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AliRocketMQAutoConfiguration.class);

    public static final String ALI_ROCKETMQ_TEMPLATE_DEFAULT_GLOBAL_NAME = "aliRocketMQTemplate";

    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkProperties() {
        String nameServer = environment.getProperty("rocketmq.ali.name-server", String.class);
        log.debug("rocketmq.ali.nameServer = {}", nameServer);
        if (nameServer == null) {
            log.warn("The necessary spring property 'rocketmq.ali.name-server' is not defined, all rockertmq beans creation are skipped!");
        }
    }

    @Bean
    @ConditionalOnMissingBean(Producer.class)
    @ConditionalOnProperty(prefix = "rocketmq.ali", value = {"name-server", "producer.access-key", "producer.secret-key"})
    public Producer producer(AliRocketMQProperties aliRocketMQProperties) {
        return ONSFactory.createProducer(RocketMQUtil.creatMQProperties(aliRocketMQProperties, environment));
    }

    @Bean
    @ConditionalOnMissingBean(OrderProducer.class)
    @ConditionalOnProperty(prefix = "rocketmq.ali", value = {"name-server", "producer.access-key"})
    @ConditionalOnExpression("${rocketmq.ali.producer.order-enable:false}")
    public OrderProducer orderProducer(AliRocketMQProperties aliRocketMQProperties) {
        return ONSFactory.createOrderProducer(RocketMQUtil.creatMQProperties(aliRocketMQProperties, environment));
    }

    /*@Bean
    @ConditionalOnMissingBean(TransactionProducer.class)
    @ConditionalOnProperty(prefix = "rocketmq.ali", value = {"name-server", "producer.access-key"})
    @ConditionalOnExpression("${rocketmq.ali.producer.transaction-enable:false}")
    public TransactionProducer transactionProducer(AliRocketMQProperties aliRocketMQProperties) {
        return ONSFactory.createTransactionProducer(RocketMQUtil.creatMQProperties(aliRocketMQProperties), new LocalTransactionCheckerImpl());
    }*/


    @Bean(destroyMethod = "destroy")
    @ConditionalOnBean(Producer.class)
    @ConditionalOnMissingBean(name = ALI_ROCKETMQ_TEMPLATE_DEFAULT_GLOBAL_NAME)
    public AliRocketMQTemplate rocketMQTemplate(Producer producer, @Autowired(required = false) OrderProducer orderProducer) {
        AliRocketMQTemplate rocketMQTemplate = new AliRocketMQTemplate();
        rocketMQTemplate.setProducer(producer);
        if (orderProducer != null) {
            rocketMQTemplate.setOrderProducer(orderProducer);
        }
        return rocketMQTemplate;
    }



}
