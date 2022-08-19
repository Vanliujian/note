# dubbo

- 今日任务：由于之前对dubbo的学习只基于理论方面，所以今天在代码上实现一下同时复习一下。

1. 为什么需要dubbo？

   随着互联网的发展，网站规模越来越大，数量越来越多，服务调用关系越来越复杂，负载均衡以及服务监控的需求迫在眉睫。所以出现了一个分布式框架来管理这些。

## 发展演变

1. 单一应用架构

2. 垂直应用架构

   - 界面+业务逻辑的实现分离
   - 应用不可能完全独立，大量应用之间需要交互

3. 分布式服务架构

   RPC：远程过程调用

4. 流动计算架构

   - 基于访问压力实时管理集群容量，提高集群利用率

## 特性

面向接口代理的高性能RPC调用

- 屏蔽了底层调用的细节

- 智能负载均衡
- 服务自动注册与发现（注册中心，相当于中介）
- 高度可扩展能力
- 运行期间流量调度（可以实现灰度发布）
- 可视化的服务治理与运维

## dubbo设计架构

架构图

<img src="https://camo.githubusercontent.com/e11a2ff9575abc290657ba3fdbff5d36f1594e7add67a72e0eda32e449508eef/68747470733a2f2f647562626f2e6170616368652e6f72672f696d67732f6172636869746563747572652e706e67" style="zoom:50%;" > 

| 节点        | 角色说明                 |
| ----------- | ------------------------ |
| `Provider`  | 服务提供者               |
| `Consumer`  | 服务消费者               |
| `Registry`  | 服务注册与发现的注册中心 |
| `Monitor`   | 监控中心                 |
| `Container` | 服务运行容器             |

容器一启动，provider会将所提供的服务信息注册到registry中。consumer启动的时候会从registry订阅所需要的服务。

当provider有变更的时候，registry可以将消息推送给consumer

## dubbo使用注册中心

推荐使用zookeeper

1. 下载安装

   - zkServer.cmd修改启动参数，需要添加的两个属性（不添加就报错，所以加上）

   ```bash
   echo on
   call %JAVA% "-Dzookeeper.admin.serverPort=9090" "-Dzookeeper.audit.enable=true"
   ```

   - 配置文件修改为zoo.cfg，修改dataDir属性

     dataDir=../data

   - 连接zookeeper客户端

     ```bash
     [zk: localhost:2181(CONNECTED) 3] ls /
     [zookeeper]
     [zk: localhost:2181(CONNECTED) 4] create  -e /aaa 123456
     Created /aaa
     [zk: localhost:2181(CONNECTED) 5] get /aaa
     123456
     ```

## dubbo使用监控中心

## dubbo-helloword

- 客户端的版本问题

  dubbo2.6以前导入zkclient操作zookeeper

  dubbo2.6及以后引入curator操作zookeeper



> 实现一个dubbo-helloword一共有三个模块（这三个模块自己起的名字）

- boot-order-service-consumer：用来消费的
- boot-user-service-provider：用来提供服务
- total-interface：整合service和JavaBean，另外两个模块引入这个模块

1. service和JavaBean

   都放在total-service下

2. 服务提供者：

   引入total-service依赖

   引入dubbo-starter依赖

   ```xml
   <dependency>
       <groupId>com.alibaba.boot</groupId>
       <artifactId>dubbo-spring-boot-starter</artifactId>
       <version>0.2.0</version>
   </dependency>
   ```

   只需要写自己的实现类，实现类上用dubbo的Service注解，用来暴露服务（后来好像升级了，变成了@DubboService）

   ```java
   import com.alibaba.dubbo.config.annotation.Service;
   import com.van.bean.UserAddress;
   import com.van.service.UserService;
   import org.springframework.stereotype.Component;
   
   import java.util.Arrays;
   import java.util.List;
   
   @Service //dubbo的service注解，暴露服务
   @Component
   public class UserServiceImpl implements UserService {
       //只是做一个简单的测试，所以直接new一个
       public List<UserAddress> getUserAddressList(String userId) {
           UserAddress address1 = new UserAddress(1, "江苏无锡", "1", "zhangsan", "17798092736", "Y");
           UserAddress address2 = new UserAddress(2, "江苏苏州", "1", "lisi", "19827394736", "Y");
           return Arrays.asList(address1,address2);
       }
   }
   ```

   application.properties配置

   ```properties
   # 服务名
   dubbo.application.name=user-service-provider
   # 服务地址
   dubbo.registry.address=127.0.0.1:2181
   # 使用的注册中心
   dubbo.registry.protocol=zookeeper
   
   # 指定通信规则，通信协议，端口号
   dubbo.protocol.name=dubbo
   dubbo.protocol.port=20880
   
   # 交给注册中心来管理
   dubbo.monitor.protocol=registry
   ```

   启动类

   - 启动类要加上@EnableDubbo注解，开启基于注解的dubbo功能

3. 服务消费者

   引入total-service依赖

   引入dubbo-starter以及引入一些其它所用到的依赖

   消费者的实现类：`使用@Reference注解注入分布式远程服务对象（现在升级为@DubboReference）`

   ```java
   @Service
   public class OrderServiceImpl implements OrderService {
       @Reference
       UserService userService;
       public List<UserAddress> initOrder(String userId) {
           List<UserAddress> userAddressList = userService.getUserAddressList(userId);
           return userAddressList;
       }
   }
   ```

   Controller中定义外部访问路径就可以拿到了

   ```java
   @Controller
   public class OrderController {
       @Autowired
       OrderService orderService;
       @ResponseBody
       @RequestMapping("/initOrder")
       public List<UserAddress> initOrder(@RequestParam("uid") String uid) {
           return orderService.initOrder(uid);
       }
   }
   ```

   同样启动类中要加@EnableDubbo注解

   消费者的配置文件

   ```properties
   dubbo.application.name=order-service-consumer
   dubbo.registry.address=zookeeper://127.0.0.1:2181
   dubbo.monitor.protocol=registry
   server.port=8088
   ```

至此一个dubbo的简单实现就完成了。

## 一些细节

1. 覆盖策略（由上到下的优先级，上面的优先级高，可以覆盖下面的配置）

   - -D	（JVM启动，-D参数优先）
   - XML  （dubbo.xml，如果在xml中配置，那么properties中重复的配置就无效）
   - Properties（dubbo.properties，application.properties优先级高于dubbo.properties）

2. 参数解释

   具体的解释可以参考官方文档

   https://dubbo.apache.org/zh/docs/v3.0/references/xml/

   比如：

   - 启动时检查（默认开启）
     - 当直接开启消费者，服务方并没有开启就会报错。如果把启动时检查关闭，那么只有当远程调用的时候才会进行检查。
   - 超时时间设置（默认一千毫秒），可以具体设置某个方法的超时时间
   - 重试次数，通常和超时设置联合使用

   **覆盖规则：**

   - 精确优先（方法级优先，接口级次之，全局配置再次之）
   - `消费者设置优先（如果级别一样，则消费方优先，提供方次之）`
   - 针对上面两个级别，精确优先的优先级大于消费者优先（比如在提供方设置了方法级别，消费方设置了接口级别。这时候就以方法级别的优先）

## 小结

今日学习dubbo演变出现的过程，以及它有哪些特性，这些特性具体的原理，原理只是了解，听了大概，具体学习在后面深入。学习dubbo的启动流程，使用zookeeper注册中心。搭建一个dubbo的简单工程，一开始是用xml配置的，后来改用了springboot的starter，针对依赖问题可以使用dependencyManagement。

