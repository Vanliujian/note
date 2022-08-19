- 今日任务：
  - Apollo进行本地配置，并拉取配置信息（接着昨天的）
  - mybatis+pagehelper分页查询

# Apollo

导入依赖

```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>1.6.2</version>
</dependency>
```

运行的时候有slf4f的警告，把slf4j依赖添加即可

获取配置信息

1. 需要在运行时，在VM options中添加-Dapp.id=SampleApp -Denv=DEV -Ddev_meta=http://localhost:8080

   ```java
   public class GetConfigTest {
       public static void main(String[] args) {
           //-Dapp.id  应用id: SampleApp
           //-Denv 环境: DEV
           //-Ddev_meta  eureka地址: http://localhost:8080
           Config appConfig = ConfigService.getAppConfig();
           String value = appConfig.getProperty("van", null);
           System.out.println(value);
       }
   }
   ```

   getAppConfig可以传入命名空间，如果不传默认是application命名空间

   ```java
   Config config = ConfigService.getConfig("micro_service.aaa");
   String aaa = config.getProperty("aaa", null);
   System.out.println(aaa);
   ```

项目管理

1. 添加部门

   通过管理员工具，系统参数，查询出organizations值，进行修改会自动覆盖

   ```bash
   [{"orgId":"TEST1","orgName":"样例部门1"},{"orgId":"TEST2","orgName":"样例部门2"},{"orgId":"micro_service","orgName":"微服务部门"}]
   ```

2. 添加集群

   新添加的集群默认会有其他集群已有的私有的namespace，但是配置是空的。

   添加集群后，后台获取参数的时候要添加集群：-Dapollo.cluster =

3. 添加用户

对于不同环境的配置

1. 可以在启动命令上添加

2. 新建apollo-env.properties文件（不同的环境）

   ```properties
   dev.meta=http://localhost:8080
   pro.meta=http://localhost:8081
   ```

## 结合springboot通过注解获取

1. 各种配置文件

   apollo-env.properties配置apollo的几种环境

   application.properties

   ```properties
   # 配置应用id
   app.id=micro_service
   # 代表springboot在启动阶段加载，这个参数设不设置均可
   apollo.bootstrap.enabled=true
   # 配置namespace，用逗号分隔
   apollo.bootstrap.namespaces=application,micro_service.aaa,micro_service.testa,TEST1.public,TEST1.public2
   server.port=63000
   ```

2. 启动类添加注解：@EnableApolloConfig

3. 其他类通过@Value注解获取

   ```java
   @RestController
   public class TestController {
       @Value("${aaa}")
       private String str;
   
       @GetMapping("/test")
       public String test() {
           return str;
       }
   }
   ```

4. 启动的时候添加参数VM options

   环境和集群（其他的参数也可以在这里设置，但是在配置文件中有过配置的就不需要再次设置了，命令行如果再次设置了应该会覆盖吧？）

   ```bash
   -Denv=DEV -Dapollo.cluster=DEFAULT
   ```

   

## 不同的环境

有几个环境需要配置几套adminserver和configserver，还有apolloconfigdb数据库。

apolloprotaldb数据库就不需要新配置了，他是管理界面，只需要部署一套。

至于需要用哪一个环境，可以在VM options中修改，也可以同时都开启：

```properties
dev.meta=http://localhost:8080
pro.meta=http://localhost:8081
```



## 灰度发布（简单了解）

为了平滑过渡的一种发布方式。比如让一部分用户使用产品特性A，一部分用户用产品特性B，如果用户对B没有什么反对意见，逐步扩大范围，把所有用户迁移到B上。





# 分页查询

mybatis+pagehelper

**如何使用**

GitHub官网：https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md

1. 添加依赖

   ```xml
   <dependency>
       <groupId>com.github.pagehelper</groupId>
       <artifactId>pagehelper</artifactId>
       <version>x.x.x</version>
   </dependency>
   ```

2. 配置PageInterceptor

   - mybatis-config.xml配置

     ```xml
     <plugins>
         <plugin interceptor="com.github.pagehelper.PageInterceptor">
             <!-- config params as the following -->
             <property name="param1" value="value1"/>
     	</plugin>
     </plugins>
     ```

   - application.xml配置

     ```xml
     <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
       <!-- other configuration -->
       <property name="plugins">
         <array>
           <bean class="com.github.pagehelper.PageInterceptor">
             <property name="properties">
               <!-- config params as the following -->
               <value>
                 param1=value1
               </value>
             </property>
           </bean>
         </array>
       </property>
     </bean>
     ```

   - 上述两者都可以，或者直接在配置类中定义

3. pagehelper参数

   默认的参数见官网，这些参数都有默认值，如果自定定义了会覆盖原来的默认值。

4. 简单使用

   - RowBounds 和 PageRowBounds（这是进行物理分页）

     方式一：

     ```java
     List<User> list = sqlSession.selectList("x.y.selectIf", null, new RowBounds(1, 10));
     ```

     - 什么是物理分页？

       使用limit限制，每次拿取到的数据只有需要的那几个

     - 什么是逻辑分页？

       一次性返回全部数据，再由程序员通过代码获取分页数据

     由于RowBounds不能获取查询总数，所以添加了PageRowBounds对象，增加了total属性，分页查询后可以获取这个属性值。

     方式二：

     在mapper中定义传参是RowBounds，调用方法的时候直接传入即可

     ```java
     List<User> selectAll(RowBounds rowBounds);
     ```

   - PageHelper.startPage静态方法

     只要在查询方法前调用startPage方法即可，紧跟着这个方法后的第一个Mybatis查询会被进行分页。

     ```java
     //获取第1页，10条内容，默认查询总数count
     PageHelper.startPage(1, 10);
     //紧跟着的第一个select方法会被分页
     List<User> list = userMapper.selectIf(1);
     ```

     同样支持request、map、pojo对象

     ```java
     //request: url?pageNum=1&pageSize=10
     //支持 ServletRequest,Map,POJO 对象，需要配合 params 参数
     PageHelper.startPage(request);
     //紧跟着的第一个select方法会被分页
     List<User> list = userMapper.selectIf(1);
     ```

     分页查询出的结果可以通过PageInfo进行包装

     ```java
     //获取第1页，10条内容，默认查询总数count
     PageHelper.startPage(1, 10);
     List<User> list = userMapper.selectAll();
     //用PageInfo对结果进行包装
     PageInfo page = new PageInfo(list);
     //PageInfo包含了非常全面的分页属性
     page.getPageNum()
     page.getPageSize()
     page.getStartRow()
     page.getEndRow()
     page.getTotal()
     page.getPages()
     page.getFirstPage()
     page.getLastPage()
     page.isFirstPage()
     page.isLastPage()
     page.isHasPreviousPage()
     page.isHasNextPage()
     ```

   - 使用参数方式

     使用参数方式需要配置supportMethodsArguments为true，以及配置params参数

     ```xml
     <plugins>
         <!-- com.github.pagehelper为PageHelper类所在包名 -->
         <plugin interceptor="com.github.pagehelper.PageInterceptor">
             <!-- 使用下面的方式配置参数，后面会有所有的参数介绍 -->
             <property name="supportMethodsArguments" value="true"/>
             <property name="params" value="pageNum=pageNumKey;pageSize=pageSizeKey;"/>
     	</plugin>
     </plugins>
     ```

     ```java
     List<User> selectByPageNumSize(
             @Param("user") User user,
             @Param("pageNumKey") int pageNum, 
             @Param("pageSizeKey") int pageSize);
     ```

     如果同时发现了pageNumKey和pageSizeKey这两个参数，就会执行分页查询。只有这两个参数存在的前提下其它分页参数才生效。

     方式二：

     如果传入的User对象中包含pageNumKey和pageSizeKey参数，这个方法也会分页。



注意点：

一般进行分页查询的时候PageHelper方法后紧跟Mybatis查询，就是安全的。

但是像下面这样就是不安全的（如果param1为null，那么其他的查询就会莫名其妙的变成分页查询）

```java
PageHelper.startPage(1, 10);
List<User> list;
if(param1 != null){
    list = userMapper.selectIf(param1);
} else {
    list = new ArrayList<User>();
}
```

我们应该把他们放一起写：如下

```java
List<User> list;
if(param1 != null) {
    PageHelper.startPage(1, 10);
    list = userMapper.selectIf(param1);
} else {
    list = new ArrayList<User>();
}
```

同样可以手动清理ThreadLocal存储的分页参数PageHelper.clearPage();



- 小结

  对于apollo的配置感觉更熟练了，集群和环境等的搭建也更熟悉，分页查询是从官网学的，今天发现从官网学习学到的很细而且讲的很好。以前学过pagehelper但是只会一个startpage方法，现在知道原来还有好几种方式，而且怎么做才能安全查询。



















