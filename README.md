# RocketMQ-Aliyun-Spring

阿里云RocketMQ SpringBoot-Starter

#### 使用方式

#### 1. 引入依赖
```xml
<dependency>
    <groupId>com.zhang</groupId>
    <artifactId>rocketmq-ali-spring-boot-starter</artifactId>
</dependency>
```

#### 2.producer配置
```yaml
 producer固定配置格式
rocketmq:
  ali:
    name-server: xxx            # nameServer  必须
    producer:
      access-key: xxx           # accessKey   必须
      secret-key: xxx           # secretKey   必须
      order-enable: false        # 非必须 用于开启发送顺序消息，默认为false关闭

# 自定义topic
producer:
  topic: test
```

**name-server**、**access-key**、**secret-key**必须要配置
**order-enable**可以不必配置，默认为false

配置完之后，可以使用@Autowired把已经初始化的**AliRocketMQTemplate**直接注入，请参考下面的引用例子：
```java
@Service
public class ProducerDemo {

    @Autowired
    private AliRocketMQTemplate rocketMQTemplate;
    
    @Value("${producer.topic}")
    private String TOPIC;

    /**
     * 普通同步消息
     */
    public void sendSync() {
        Message message = new Message();
        message.setBody("hello world!!!".getBytes());
        message.setTopic(TOPIC);
        SendResult result = rocketMQTemplate.sendSync(message);
        System.out.println(result.getMessageId());
    }
    .....
}
```
#### 3.consumer配置
##### 消费者配置：
```yml
demo:
  consumer:
    test:
      nameServer: xxx          # nameServer  必须
      accessKey: xxx           # accessKey   必须
      secretKey: xxx           # secretKey   必须
      groupId: GID_TEST        # groupId     必须，并且以GID_或者GID-开命名开头
      topic: test              # topic       必须
      tag: '*'                 # tag         非必须
```
配置完成后，要创建自定义类 并且实现 **AliRocketMQListener** 接口和配置 **AliRocketMQMessageListener** 注解，请参考下面的例子：
```java
@Component
@AliRocketMQMessageListener(
        nameServer = "${demo.consumer.test.nameServer}",
        accessKey = "${demo.consumer.test.accessKey}",
        secretKey = "${demo.consumer.test.secretKey}",
        groupId = "${demo.consumer.test.groupId}",
        topic = "${demo.consumer.test.topic}",
        expression = "${demo.consumer.test.tag}")
public class ConsumerDemo implements AliRocketMQListener {

    @Override
    public Action consume(Message message, ConsumeContext context) {
        System.out.println("#### Receive Message ####");
        System.out.println("message id: " + message.getMsgID());
        System.out.println("topic: [" + message.getTopic() + "] - tag: [" + message.getTag() + "]");
        System.out.println("body: [" + new String(message.getBody()) + "]");
        System.out.println("#########################");
        return Action.CommitMessage;
    }
}
```

注解中使用了占位符的配置方式，使配置更加便捷。

要注意，注解中的配置参数要与配置文件中的参数一一对应


