package com.zhang.rocketmq.aliyun.spring.autoconfigure;

import com.aliyun.openservices.ons.api.MessageListener;
import com.zhang.rocketmq.aliyun.spring.annotation.AliRocketMQMessageListener;
import com.zhang.rocketmq.aliyun.spring.core.AliRocketMQListener;
import com.zhang.rocketmq.aliyun.spring.support.DefaultAliRocketMQListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author : zzh
 * create at:  2020/4/16
 * @description:
 */
@Configuration
public class ListenerContainerConfiguration implements ApplicationContextAware, SmartInitializingSingleton {

    private final static Logger log = LoggerFactory.getLogger(ListenerContainerConfiguration.class);

    public static final String GROUP_ID_PREFIX = "GID";

    private ConfigurableApplicationContext applicationContext;

    private AtomicLong counter = new AtomicLong(0);

    private StandardEnvironment environment;

    private AliRocketMQProperties aliRocketMQProperties;

    public ListenerContainerConfiguration(StandardEnvironment environment, AliRocketMQProperties aliRocketMQProperties) {
        this.environment = environment;
        this.aliRocketMQProperties = aliRocketMQProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(AliRocketMQMessageListener.class)
                .entrySet()
                .stream()
                .filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        beans.forEach(this::registerContainer);
    }

    private void registerContainer(String beanName, Object bean) {
        // 获取代理对象的最终类型
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (!MessageListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " cannot be both instance of " + MessageListener.class.getName());
        }

        AliRocketMQMessageListener annotation = clazz.getAnnotation(AliRocketMQMessageListener.class);

        String containerBeanName = String.format("%s_%s", DefaultAliRocketMQListenerContainer.class.getName(),
                counter.incrementAndGet());
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        genericApplicationContext.registerBean(containerBeanName, DefaultAliRocketMQListenerContainer.class,
                () -> createAliRocketMQListenerContainer(containerBeanName, bean, annotation));
        DefaultAliRocketMQListenerContainer container = genericApplicationContext.getBean(containerBeanName,
                DefaultAliRocketMQListenerContainer.class);
        if (!container.isRunning()) {
            try {
                container.start();
            } catch (Exception e) {
                log.error("Started container failed. {}", container, e);
                throw new RuntimeException(e);
            }
        }
        log.info("Register the listener to container, listenerBeanName:{}, containerBeanName:{}", beanName, containerBeanName);
    }

    private DefaultAliRocketMQListenerContainer createAliRocketMQListenerContainer(String name, Object bean, AliRocketMQMessageListener annotation) {
        DefaultAliRocketMQListenerContainer container = new DefaultAliRocketMQListenerContainer();

        container.setAliRocketMQMessageListener(annotation);
        // name-server
        String nameServer = environment.resolvePlaceholders(annotation.nameServer());
        nameServer = StringUtils.isEmpty(nameServer) ? aliRocketMQProperties.getNameServer() : nameServer;
        // access-key
        String accessKey = environment.resolvePlaceholders(annotation.accessKey());
        accessKey = StringUtils.isEmpty(accessKey) ? aliRocketMQProperties.getAccessKey() : accessKey;
        // secret-key
        String secretKey = environment.resolvePlaceholders(annotation.secretKey());
        secretKey = StringUtils.isEmpty(secretKey) ? aliRocketMQProperties.getSecretKey() : secretKey;

        if (StringUtils.isEmpty(nameServer) || StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(secretKey)) {
            log.error("[`nameServer`, `accessKey`, `secretKey`] cannot be null");
            throw new IllegalArgumentException("[`nameServer`, `accessKey`, `secretKey`] cannot be null");
        }
        container.setNameServer(nameServer);
        container.setAccessKey(accessKey);
        container.setSecretKey(secretKey);
        // consumer-id
        String groupId = environment.resolvePlaceholders(annotation.groupId());
        if (StringUtils.isEmpty(groupId) || !groupId.startsWith(GROUP_ID_PREFIX)) {
            log.error("`consumerGroup` is null or format error [must start with {}]", GROUP_ID_PREFIX);
            throw new IllegalArgumentException("`consumerGroup` is null or format error [must start with " + GROUP_ID_PREFIX +"]");
        }
        container.setGroupId(groupId);
        container.setTopic(environment.resolvePlaceholders(annotation.topic()));
        String tags = environment.resolvePlaceholders(annotation.expression());
        if (!StringUtils.isEmpty(tags)) {
            container.setExpression(tags);
        }
        if (AliRocketMQListener.class.isAssignableFrom(bean.getClass())) {
            container.setAliRocketMQListener((AliRocketMQListener) bean);
        }
        container.setName(name);
        return container;
    }
}
