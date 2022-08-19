- 任务：学习RocketMQ，并了解怎样使用

# RocketMQ

## 下载安装

...

需要先启动nameser再启动broker

## 初识

1. 什么是消息中间件

   分布式系统中完成消息的发送和接收的基础软件。基于队列与消息传递技术。

2. 消息中间件的使用场景

   异步与解耦

   流量削峰

   数据分发

## 拥有的角色

<a href="https://sm.ms/image/RqB14ViF7XLkxwM" target="_blank"><img src="https://s2.loli.net/2022/08/15/RqB14ViF7XLkxwM.png" style="zoom:70%;"  ></a> 

- NameSever：

  注册中心（相当于zookeeper）

  接收broker的请求，注册broker的路由信息

  接收client（producer/consumer）的请求，根据某个topic获取其到broker的路由信息；
  NameServer没有状态，可以横向扩展。每个broker在启动的时候会到NameServer注册；
  Producer在发送消息前会根据topic到NameServer获取路由(到broker)信息；
  Consumer也会定时获取topic路由信息。

- Broker：消息存储中心，接收来自Producer的消息并存储

  消息中转角色，负责存储消息，转发消息。可以理解为消息队列服务器，提供了消息的接收、存储、拉取和转发服务。broker是RocketMQ的核心，它不能挂的，所以需要保证broker的高可用。broker分为 Master Broker 和 Slave Broker，一个 Master Broker 可以对应多个 Slave Broker，但是一个 Slave Broker 只能对应一个 Master Broker。

- Producer：

  消息发布者

  与 Name Server 集群中的其中一个节点（随机）建立长链接（Keep-alive），定期从 Name Server 读取 Topic 路由信息，并向提供 Topic 服务的 Master Broker 建立长链接，且定时向 Master Broker 发送心跳。

- Consumer：

  消息订阅者

  与 Name Server 集群中的其中一个节点（随机）建立长连接，定期从 Name Server 拉取 Topic 路由信息，并向提供 Topic 服务的 Master Broker、Slave Broker 建立长连接，且定时向 Master Broker、Slave Broker 发送心跳。Consumer 既可以从 Master Broker 订阅消息，也可以从 Slave Broker 订阅消息，订阅规则由 Broker 配置决定。

## 基本概念

- Topic：主题，用在broker中，下面有一些队列来存储信息
- Group：分组，消费者和生产者都可以分组
- OffSet：偏移量，管理每个消息队列消费进度（生产者生产到哪里，消费者消费到哪里）

## 消息发送

### 普通消息发送

1. 同步发送

   像短信一样

   ```java
   public class SyncProducerTest {
       public static void main(String[] args) throws Exception {
           //实例化消息生产者
           DefaultMQProducer mqProducer = new DefaultMQProducer("group_test");
           //设置nameserver地址
           mqProducer.setNamesrvAddr("127.0.0.1:9876");
           mqProducer.setSendLatencyFaultEnable(true);
   
           //启动producer实例
           mqProducer.start();
           for (int i = 0; i < 10; i++) {
               Message message = new Message("TopicTest", "TagA", ("Hello " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
               //发送一个消息到Broker,同步发送
               SendResult send = mqProducer.send(message);
               System.out.printf("%s%n",send);
           }
           mqProducer.shutdown();
       }
   }
   ```

2. 异步发送

   异步响应（适合发送数据量比较大，不能容忍阻塞场景）

   ```java
   public class AsyncProducer {
       public static void main(String[] args) throws Exception {
           //实例化消息生产者
           DefaultMQProducer mqProducer = new DefaultMQProducer("group_test");
           //设置nameserver地址
           mqProducer.setNamesrvAddr("127.0.0.1:9876");
           mqProducer.setSendLatencyFaultEnable(true);
   
           //启动producer实例
           mqProducer.start();
           for (int i = 0; i < 10; i++) {
               final int index = i;
               Message message = new Message("TopicTest", "TagA", "OrderID", "Hello ".getBytes(RemotingHelper.DEFAULT_CHARSET));
               //发送一个消息到Broker,接收异步返回结果的回调
               mqProducer.send(message, new SendCallback() {
                   public void onSuccess(SendResult sendResult) {
                       System.out.printf("%s%n", sendResult);
                   }
   
                   public void onException(Throwable throwable) {
                       System.out.println(String.valueOf(index) + throwable);
                   }
               });
           }
           Thread.sleep(10000);
           mqProducer.shutdown();
       }
   }
   ```

3. 单向发送

   不需要得到确认，直接发送（不知道是否发送成功）

   ```java
   public class OnewayProducer {
       public static void main(String[] args) throws Exception {
           //实例化消息生产者
           DefaultMQProducer mqProducer = new DefaultMQProducer("group_test");
           //设置nameserver地址
           mqProducer.setNamesrvAddr("127.0.0.1:9876");
           mqProducer.setSendLatencyFaultEnable(true);
   
           //启动producer实例
           mqProducer.start();
           for (int i = 0; i < 10; i++) {
               Message message = new Message("TopicTest", "TagA", ("Hello " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
               //发送单向消息，没有返回结果
               mqProducer.sendOneway(message);
           }
           mqProducer.shutdown();
       }
   }
   ```

### 普通消息的消费模式

1. 集群消费

   ```java
   public class BalanceConsumer {
       public static void main(String[] args) throws Exception{
           DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group_consumer");
           consumer.setNamesrvAddr("127.0.0.1:9876");
           //订阅topic
           consumer.subscribe("TopicTest","*");
   
           //集群模式消费
           consumer.setMessageModel(MessageModel.CLUSTERING);
           //注册回调函数处理消息
           consumer.registerMessageListener(new MessageListenerConcurrently() {
               public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                   try {
                       for (MessageExt msg : msgs) {
                           String topic = msg.getTopic();
                           String msgBody = new String(msg.getBody(), "utf-8");
                           String tags = msg.getTags();
                           System.out.println("收到消息：Topic "+ topic + ",Tags: "+tags+",msgBody: "+msgBody);
                       }
                   } catch (Exception e) {
                       e.printStackTrace();
                       return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                   }
                   return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
               }
           });
           consumer.start();
           
       }
   }
   ```

2. 广播消费

   把模式改一下即可：MessageModel.BROADCASTING

   消费者进度保存在客户端，可能会多次消费





- 顺序消息

  全局顺序消息：一个主题对应一个Queue

  部分顺序消息：一个主题下面有多个Queue，但是每个Queue都固定向某个生产者和消费者提供服务。用一定的标识分开。

- 延时消息

  在创建出一个Message后，通过setDelayTimeLevel设置消费等级

  分为1-18个等级分别表示 1s、5s、10s、30s、1m、2m、3m、4m、5m、6m、7m、8m、9m、10m、20m、30m、1h、2h

  会在这么长的时间后才发送消息

- 批量消息

  通过集合来发送，把Message存储在集合中。

- 过滤消息

  订阅的时候加一个过滤表达式

  Tag过滤、SQL过滤

## 分布式事务

进行事务回查（默认60s一次）



## DefaultMQProducer

属性：

| 字段类型              | 字段名称                         | 描述                                                         |
| --------------------- | -------------------------------- | ------------------------------------------------------------ |
| DefaultMQProducerImpl | defaultMQProducerImpl            | 生产者内部默认实现类                                         |
| String                | producerGroup                    | 生产组聚合所有生产者进程，对于非事务生产消息，可在多个进程定义相同生产组；然而事务消息必须与非事务消息生产组区分。 |
| String                | createTopicKey                   | 自动创建测试的topic名称；broker必须开启isAutoCreateTopicEnable |
| int                   | defaultTopicQueueNums            | 创建默认topic的queue数量。默认4                              |
| int                   | sendMsgTimeout                   | 发送消息超时时间(ms)，默认3000ms                             |
| int                   | compressMsgBodyOverHowmuch       | 消息体压缩阈值，默认为4k。                                   |
| int                   | retryTimesWhenSendFailed         | 同步模式，返回发送消息失败前内部重试发送的最大次数。可能导致消息重复。默认2 |
| int                   | retryTimesWhenSendAsyncFailed    | 异步模式，返回发送消息失败前内部重试发送的最大次数。可能导致消息重复。默认2 |
| boolean               | retryAnotherBrokerWhenNotStoreOK | 声明发送失败时，下次是否投递给其他Broker，默认false          |
| int                   | maxMessageSize                   | 最大消息大小。默认4M                                         |
| TraceDispatcher       | traceDispatcher                  | 消息追踪器，定义了异步传输数据接口。使用rcpHook来追踪消息    |



构造方法：

| 方法名称                                                     | 方法描述                                                     |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| DefaultMQProducer()                                          | 由默认参数值创建一个生产者                                   |
| DefaultMQProducer(final String producerGroup)                | 使用指定的分组名创建一个生产者                               |
| DefaultMQProducer(final String producerGroup, boolean enableMsgTrace) | 使用指定的分组名创建一个生产者，并设置是否开启消息轨迹       |
| DefaultMQProducer(final String producerGroup, boolean enableMsgTrace, final String customizedTraceTopic) | 使用指定的分组名创建一个生产者，并设置是否开启消息轨迹及追踪topic的名称 |
| DefaultMQProducer(RPCHook rpcHook)                           | 使用指定的hook创建一个生产者                                 |
| DefaultMQProducer(final String producerGroup, RPCHook rpcHook) | 使用指定的分组名及自定义hook创建一个生产者                   |
| DefaultMQProducer(final String producerGroup, RPCHook rpcHook, boolean enableMsgTrace,final String customizedTraceTopic) | 使用指定的分组名及自定义hook创建一个生产者，并设置是否开启消息轨迹及追踪topic的名称 |



使用方法：

| 返回值                | 方法名称                                                     | 方法描述                                                   |
| --------------------- | ------------------------------------------------------------ | ---------------------------------------------------------- |
| void                  | createTopic(String key, String newTopic, int queueNum)       | 在broker上创建指定的topic                                  |
| void                  | createTopic(String key, String newTopic, int queueNum, int topicSysFlag) | 在broker上创建指定的topic                                  |
| long                  | earliestMsgStoreTime(MessageQueue mq)                        | 查询最早的消息存储时间                                     |
| List                  | fetchPublishMessageQueues(String topic)                      | 获取topic的消息队列                                        |
| long                  | maxOffset(MessageQueue mq)                                   | 查询给定消息队列的最大offset                               |
| long                  | minOffset(MessageQueue mq)                                   | 查询给定消息队列的最小offset                               |
| QueryResult           | queryMessage(String topic, String key, int maxNum, long begin, long end) | 按关键字查询消息                                           |
| long                  | searchOffset(MessageQueue mq, long timestamp)                | 查找指定时间的消息队列的物理offset                         |
| SendResult            | send(Collection msgs)                                        | 同步批量发送消息                                           |
| SendResult            | send(Collection msgs, long timeout)                          | 同步批量发送消息                                           |
| SendResult            | send(Collection msgs, MessageQueue messageQueue)             | 向指定的消息队列同步批量发送消息                           |
| SendResult            | send(Collection msgs, MessageQueue messageQueue, long timeout) | 向指定的消息队列同步批量发送消息，并指定超时时间           |
| SendResult            | send(Message msg)                                            | 同步单条发送消息                                           |
| SendResult            | send(Message msg, long timeout)                              | 同步发送单条消息，并指定超时时间                           |
| SendResult            | send(Message msg, MessageQueue mq)                           | 向指定的消息队列同步发送单条消息                           |
| SendResult            | send(Message msg, MessageQueue mq, long timeout)             | 向指定的消息队列同步单条发送消息，并指定超时时间           |
| void                  | send(Message msg, MessageQueue mq, SendCallback sendCallback) | 向指定的消息队列异步单条发送消息，并指定回调方法           |
| void                  | send(Message msg, MessageQueue mq, SendCallback sendCallback, long timeout) | 向指定的消息队列异步单条发送消息，并指定回调方法和超时时间 |
| SendResult            | send(Message msg, MessageQueueSelector selector, Object arg) | 向消息队列同步单条发送消息，并指定发送队列选择器           |
| SendResult            | send(Message msg, MessageQueueSelector selector, Object arg, long timeout) | 向消息队列同步单条发送消息，并指定发送队列选择器与超时时间 |
| void                  | send(Message msg, MessageQueueSelector selector, Object arg, SendCallback sendCallback) | 向指定的消息队列异步单条发送消息                           |
| void                  | send(Message msg, MessageQueueSelector selector, Object arg, SendCallback sendCallback, long timeout) | 向指定的消息队列异步单条发送消息，并指定超时时间           |
| void                  | send(Message msg, SendCallback sendCallback)                 | 异步发送消息                                               |
| void                  | send(Message msg, SendCallback sendCallback, long timeout)   | 异步发送消息，并指定回调方法和超时时间                     |
| TransactionSendResult | sendMessageInTransaction(Message msg, LocalTransactionExecuter tranExecuter, final Object arg) | 发送事务消息，并指定本地执行事务实例                       |
| TransactionSendResult | sendMessageInTransaction(Message msg, Object arg)            | 发送事务消息                                               |
| void                  | sendOneway(Message msg)                                      | 单向发送消息，不等待broker响应                             |
| void                  | sendOneway(Message msg, MessageQueue mq)                     | 单向发送消息到指定队列，不等待broker响应                   |
| void                  | sendOneway(Message msg, MessageQueueSelector selector, Object arg) | 单向发送消息到队列选择器的选中的队列，不等待broker响应     |
| void                  | shutdown()                                                   | 关闭当前生产者实例并释放相关资源                           |
| void                  | start()                                                      | 启动生产者                                                 |
| MessageExt            | viewMessage(String offsetMsgId)                              | 根据给定的msgId查询消息                                    |
| MessageExt            | public MessageExt viewMessage(String topic, String msgId)    | 根据给定的msgId查询消息，并指定topic                       |



## DefaultMQPushConsumer

官方文档API更新中



- 小结：rocketmq的基本信息了解了，但是用起来很空洞，主要是没有实际场景，不知道怎么使用。还有一些没有学完。












## 高可用

### 集群部署模式

- 单master模式
- 多master模式
- 多master多slave模式（同步）
- 多master多slave模式（异步）

### 刷盘与主从同步

- 同步刷盘与异步刷盘
- 同步复制与异步复制

### RocketMQ的存储设计

<a href="https://sm.ms/image/3uk42QoYIaKdOMy" target="_blank"><img src="https://s2.loli.net/2022/08/15/3uk42QoYIaKdOMy.png" style="zoom:80%;"  ></a> 

消息存储结构

commitlog：消息存储目录，每个commitlog大小大概为1个G

config：运行期间的一些配置信息

consumequeue：消息消费队列存储目录

index：消息索引文件存储目录

abort：如果存在改文件，则broker非正常关闭

checkpoint：文件检查点，存储Commitlog文件最后一次刷盘时间戳、consumerqueue最后一次刷盘时间，index索引最后一次刷盘时间戳。

<a href="https://sm.ms/image/uExzyYJAPo6nscW" target="_blank"><img src="https://s2.loli.net/2022/08/15/uExzyYJAPo6nscW.png" style="zoom:70%;"  ></a>

### 过期文件删除

非当前写文件在一定时间内没有再次被更新，则认为是过期文件，可以删除，不会关注这个文件上的消息是否全被消费。

### 分布式事务

<a href="https://sm.ms/image/KxDsEnwSHcJdQVL" target="_blank"><img src="https://s2.loli.net/2022/08/15/KxDsEnwSHcJdQVL.png" ></a>

实现TransactionListener接口



### 负载均衡

Producer：Roundbin方式轮询

Cunsumer：

- AllocateMessageQueueAveragely（默认）

  按照顺序，平均分配，比如有6个队列，2个consumer，consumer1拿前三个，consumer2拿后三个

- AllocateMessageQueueAveragelyByCircle

  轮询，第一次每个consumer拿一个，第二次循环也是，一直循环下去，比如有6个队列，2个consumer，consumer1拿queue1，consumer2拿queue2，consumer1继续拿queue3，consumer2拿queue4...