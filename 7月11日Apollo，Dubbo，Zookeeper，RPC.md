# 分布式配置中心Apollo

## 读取配置

-Dapp.id=应用名称

-Denv=环境名称

-Dapollo.cluster=集群名称

-D环境_meta=meta地址



Apollo配置是为了一些经常变的配置，解决了不用修改配置就要发布的问题



# RPC远程过程调用

remote procedure call

RPC是基于分布式架构出现的，所以在分布式上有优势。



比如有两个服务，服务1调用服务2提供的用户列表接口，服务1里这样就叫做一次rpc

```java
String userListStr = restTemplate.getForObject("http://service2.hailan.com/userList",String.class);
```



# Dubbo框架

轻量级RPC框架

地址缓存

超时与重试 timeout 和 retries属性

负载均衡

集群容错

服务降级

Dubbo执行流程

- 第一步：provider 向注册中心去注册
- 第二步：consumer 从注册中心订阅服务，注册中心会通知 consumer 注册好的服务
- 第三步：consumer 调用 provider
- 第四步：consumer 和 provider 都异步通知监控中心

常规系统中提供服务是通过@Controller与@RequestMapping来定义的，dubbo中直接使用@Service就可以了。

```java
@Reference
Service2 service2;
public void method() {
    List<User> list = service2.userList();
}
```



# Zookeeper

Zookeeper是一个分布式协调技术、高性能的，开源的分布式系统的协调(Coordination)服务

统一命名服务

配置管理

集群管理



> 为什么dubbo推荐使用zk做注册中心？

主要是因为zk是CP模型，并且zk的watch机制特别适合服务发现。现在不管什么中间件，只要涉及到服务发现基本都是zk，特别是大数据下的很多数据库和框架