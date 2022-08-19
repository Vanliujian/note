- 今日任务：理解RPC在架构中的位置，用于解决的问题，下载单机部署zookeeper，了解zookeeper常用命令

# RPC

## RFC

是一个文件集（请求描述的语言集），其中就收集了rpc



## RPC

在rpc协议中强调当A程序调用B程序中功能或方法时，A是不知道B中的方法实现的



## RPC和HTTP对比

RPC：可以基于TCP协议，也可以基于HTTP协议。自定义具体实现可以减少很多无用的报文内容，使得报文体积更小

HTTP：基于HTTP协议。http1.0和1.1中很多报文都是无用的。HTTP2.0以后和RPC相差不大，比RPC少的可能是服务治理等功能。

- 连接方式

RPC：支持长连接

HTTP：每次连接都是3次握手，4次挥手（2.0也支持长连接）

- 负载均衡

RPC：绝大多数RPC框架都带有负载均衡

HTTP：一般都需要第三方工具。如nginx

## HTTPClient实现RPC

httpclient_server模块

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>2.7.2</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>2.7.2</version>
    </dependency>
</dependencies>
```

提供一个controller和start类

```java
@Controller
public class TestController {

    @RequestMapping("/test")
    @ResponseBody
    public String test() {
        return "msg:处理返回";
    }

    @RequestMapping("/param")
    @ResponseBody
    public String test2(String name,String password) {
        return "msg:处理返回"+"name-"+name+" password-"+password;
    }
}
```

httpclient_client模块

```xml
<dependencies>
<!--        httpclient依赖-->
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.13</version>
        </dependency>
    </dependencies>
```

测试方法

```java
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TestHttpClient {
    public static void main(String[] args) throws Exception {
//        testGet();
        testGetParam();
    }


    //无参get请求
    public static void testGet() throws IOException {
        //创建客户端对象
        HttpClient client = HttpClients.createDefault();
        //创建请求地址
        HttpGet httpGet = new HttpGet("http://localhost/test");
        //发起请求，接收响应体
        HttpResponse response = client.execute(httpGet);
        //获取响应体
        HttpEntity entity = response.getEntity();

        String string = EntityUtils.toString(entity);
        System.out.println(string);
    }

    public static void testGetParam() throws URISyntaxException, IOException {
        HttpClient client = HttpClients.createDefault();
        URIBuilder builder = new URIBuilder("http://localhost/param");
//        基于单参传递
//        builder.addParameter("name","zhang");
//        builder.addParameter("password","123");

        //基于多参传递
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("name","zhnag"));
        list.add(new BasicNameValuePair("password","123"));
        builder.addParameters(list);

        URI uri = builder.build();
        HttpResponse response = client.execute(new HttpGet(uri));
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);
        System.out.println(result);
    }
    
    public static void testPost() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost/test");
        CloseableHttpResponse response = client.execute(httpPost);
        String str = EntityUtils.toString(response.getEntity());
        System.out.println(str);

        //有参post
        URIBuilder builder = new URIBuilder("http://localhost/param");
        builder.addParameter("name","lisi");
        builder.addParameter("password","1234");
        CloseableHttpResponse closeableHttpResponse = client.execute(new HttpPost(builder.build()));
        String str2 = EntityUtils.toString(closeableHttpResponse.getEntity());
        System.out.println(str2);
    }

}
```



# Zookeeper

- zookeeper是apache hadoop项目下的一个子项目，是一个树形目录服务

- 提供的功能
  - 配置管理
  - 分布式锁
  - 集群管理

## 单机安装部署

## 常用命令

> zookeeper数据模型

zookeeper是一个树形目录服务，每一个节点都被称为ZNode，每个节点上都会保存自己的数据和节点信息，节点可以拥有子节点，也可以保存少量数据信息

节点可以分为四大类：

- PERSISTENT持久化节点
- EPHEMERAL临时节点：-e
- PERSISTENT_SEQUENTIAL持久化顺序节点：-s
- EPHEMERAL_SEQUENTIAL临时顺序节点：-es

zookeeper服务端命令(开启、关闭)

```shell
./zkServer.sh start
./zkServer.sh stop
```

客户端常用命令

quit：断开连接

set /节点path value：设置节点

delete /节点path：删除单个节点

deleteall /节点path：删除带有子节点的节点

create /节点path value：创建节点

get /节点path：获取节点值

```shell
# 连接到服务端
./zkCli.sh -server localhost:2182

[zk: localhost:2181(CONNECTED) 0] ls /
[dubbo, zookeeper]
[zk: localhost:2181(CONNECTED) 1] ls /dubbo
[com.alibaba.dubbo.monitor.MonitorService, com.van.service.UserService]
[zk: localhost:2181(CONNECTED) 2]
[zk: localhost:2181(CONNECTED) 2] create  /app1 heilan
Created /app1
[zk: localhost:2181(CONNECTED) 3] get /app1
heilan
[zk: localhost:2181(CONNECTED) 4] set /app1 hailan
[zk: localhost:2181(CONNECTED) 5] get /app1
hailan
[zk: localhost:2181(CONNECTED) 6] delete /app1
[zk: localhost:2181(CONNECTED) 7] ls /
[dubbo, zookeeper]
[zk: localhost:2181(CONNECTED) 8] create /app1/p1
Node does not exist: /app1/p1
[zk: localhost:2181(CONNECTED) 9] create /app1
Created /app1
[zk: localhost:2181(CONNECTED) 10] create /app1/p1
Created /app1/p1
[zk: localhost:2181(CONNECTED) 11] delete /app1
Node not empty: /app1
[zk: localhost:2181(CONNECTED) 13] deleteall /app1
[zk: localhost:2181(CONNECTED) 14] ls /
[dubbo, zookeeper]
[zk: localhost:2181(CONNECTED) 15]
```



create -e /节点path value：创建临时节点

create -s /节点path value：创建顺序节点

ls -s /节点path：查询节点详细信息

## ZookeeperJavaAPI操作

### Curator

curator是apache zookeeper的Java客户端

> 建立连接

```java
public class CuratorTest {

    @Test
    public void testConnect() {
        //重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000,10);
        /**
         * new Client方法
         * @param connectString       连接字符串，zkserver地址和端口
         * @param sessionTimeoutMs    会话超时时间
         * @param connectionTimeoutMs 连接超时时间
         * @param retryPolicy         重试策略
         * @return client
         */
        //第一种方式
//        CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.10.1",
//                60 * 1000, 15 * 1000, retryPolicy);
        //第二种方式
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.10.1")
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(15 * 1000)
                .retryPolicy(retryPolicy).namespace("app1").build();

        client.start();

    }
}
```

> 创建节点

```java
public class CuratorTest {

    CuratorFramework client;
    @Before
    public void testConnect() {
        //......
        client.start();
    }

    @Test
    public void testCreate() throws Exception {
        //如果创建节点没有指定数据，就将本机ip地址作为数据
        client.create().forPath("/app1");
        //带有数据的创建
        client.create().forPath("/app2","hello".getBytes());
        //默认类型是持久化，如果要别的类型，可以withMode设置
        client.create().withMode(CreateMode.EPHEMERAL).forPath("/app3");

        //如果父节点不存在就创建节点
        client.create().creatingParentsIfNeeded().forPath("/app4");
    }

    @After
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
```

> 查询节点

```java
/*
查询节点
1. 查询数据 get
2. 查询子节点 ls
3. 查询节点状态信息 ls -s
 */
@Test
public void testGet1() throws Exception {
    //查询数据，get
    byte[] bytes = client.getData().forPath("/app1");
    System.out.println(new String(bytes));
}

@Test
public void testGet2() throws Exception {
    //查询所有子节点（namespace下）
    List<String> path = client.getChildren().forPath("/");
    System.out.println(path);
}

@Test
public void testGet3() throws Exception {
    //查询节点状态信息
    Stat stat = new Stat();
    client.getData().storingStatIn(stat).forPath("/app1");
    System.out.println(stat);
}
```

> 修改节点

```java
//修改节点
//1. 直接修改
//2. 根据版本号修改
@Test
public void testUpdate() throws Exception {
    //直接修改（有并发危险）
    client.setData().forPath("/app1","haha".getBytes());
    //根据版本修改
    Stat stat = new Stat();
    client.getData().storingStatIn(stat).forPath("/app1");
    client.setData().withVersion(stat.getVersion()).forPath("/app1", "heihei".getBytes());
}
```

> 删除节点

```java
/**
 * 删除节点
 * 1. 删除单个子节点
 * 2. 删除带有子节点的节点
 * 3. 必须成功的删除
 * 4. 回调
 */
@Test
public void testDelete() throws Exception{
    client.delete().forPath("/app1");
}
@Test
public void testDelete2() throws Exception{
    //2.删除带有子节点的节点
    client.delete().deletingChildrenIfNeeded().forPath("/app2");
}
@Test
public void testDelete3() throws Exception{
    //必须成功的删除
    client.delete().guaranteed().forPath("/app1");
}
@Test
public void testDelete4() throws Exception{
    //回调
    client.delete().guaranteed().inBackground(new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
            System.out.println("回调函数");
        }
    }).forPath("/app1");
}
```

### Watch事件监听

zookeeper允许用户在指定节点上注册一些Watcher，并且在一些特定事件触发的时候，zookeeper服务端会将事件通知到感兴趣的客户端上去。

zookeeper引入watcher机制，能够让多个订阅者同时监听一个对象，当对象发生变化时，会通知所有订阅者。

Curator引入Cache实现对zookeeper服务端事件监听。

zookeeper提供了三种Watcher

- NodeCache：只是监听某一个特定的节点
- PathChildrenCache：监控一个ZNode的子节点
- TreeCache：可以监控整个树上的所有节点（类似上面两个的整合）

这个NodeCache显示不建议被使用，那应该用什么？

```java
/**
 * NodeCache:给指定一个节点注册监听器
 */
@Test
public void testNodeCache() throws Exception {
    //1. 创建NodeCache对象
    final NodeCache nodeCache = new NodeCache(client,"/app1");
    //2. 注册监听
    nodeCache.getListenable().addListener(new NodeCacheListener() {
        @Override
        public void nodeChanged() throws Exception {
            System.out.println("节点变化了~~");
            //获取修改节点后的数据
            byte[] data = nodeCache.getCurrentData().getData();
            System.out.println(new String(data));
        }
    });
    nodeCache.start(true);
}
```

监控一个节点的子节点（不包括自己）

```java
@Test
public void testNodeCache2() throws Exception {
    //1. 创建NodeCache对象
    PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/app2", true);
    //绑定监听器
    pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
        @Override
        public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
            System.out.println("子节点变化了");
            System.out.println(event);
            //监听子节点数据变更，拿到变更后的数据
            PathChildrenCacheEvent.Type type = event.getType();
            //判断类型是否是update
            if (type.equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                System.out.println("数据变了");
                byte[] data = event.getData().getData();
                System.out.println(new String(data));
            }
        }
    });
    pathChildrenCache.start();
}
```

监控自己包括自己的所有子孙节点（代码差不多）

```java
@Test
public void testNodeCache3() throws Exception {
    //1. 创建NodeCache对象
    TreeCache treeCache = new TreeCache(client, "/app1");

    //绑定监听器
    treeCache.getListenable().addListener(new TreeCacheListener() {
        @Override
        public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
            System.out.println("节点变化了");
        }
    });
    treeCache.start();
}
```

## 分布式锁

处理跨机器的进程之间的数据同步问题

zookeepper分布式锁原理

- 当客户端要获取锁，就创建节点，使用完锁则删除节点

1. 客户端获取锁时，在lock节点下创建临时顺序节点（如果客户端宕机了，它会自动删除）
2. 然后获取lock下面所有子节点，客户端获取到所有子节点后，如果发现自己的子节点序号最小，就认为客户端获取到了锁。使用完锁，将节点删除
3. 如果发现自己创建的节点不是最小，客户端需要找到比自己小的那个节点（比自己小的那一个节点），同时对其注册事件监听，监听删除事件。
4. 如果比自己小的节点被删除，客户端的Watcher会收到通知，此时再次判断自己创建的节点是否是最小的。

## zookeeper集群

- leader选举机制
- ......



> 小结

今天学习了RPC、Zookeeper常用命令，以及基本知识点。

zookeeper的监听中心好像不建议被使用了，NodeCache、PathChildrenCache、TreeCache，如果不用这三个，那现在用的是什么？

