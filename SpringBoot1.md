# SpringBoot学习

> Spring缺点

- 配置繁琐
- 依赖繁琐

> SpringBoot功能

- 自动配置
- 起步依赖
- 辅助功能

提供了一种快速开发Spring项目的方式，而不是对Spring功能上的增强



>springboot简单搭建

Maven构建

```xml
//pom.xml依赖文件
<!--spring工程所需要继承的父工程-->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.8.RELEASE</version>
</parent>
<dependencies>
    <!--web开发的起步依赖-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

```java
//Controller代码
@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        return "Hello Springboot";
    }
}
```

```java
/*
 *引导类，springboot项目的入口
 */
@SpringBootApplication
public class HelloApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class);
    }
}
```

使用idea直接创建springboot项目速度更快

> SpringBoot起步依赖原理分析

spring-boot-start-parent中定义了各种版本信息

各种starter中，定义了完成该功能需要的合集，其中大部分版本信息来自父工程

我们的工程继承parent，引入starterhh后，通过依赖传递可以获取jar包



> 配置

1. 配置文件分类：

   - properties
   - yml（yaml）

   程序对于有的配置会有自动识别，比如sever.port=8081，这样就会改端口号变为8081

   对于自己写的配置需要手动读取

2. 优先级

   如果三个文件都存在，且properties、yml、yaml三个文件都配置了端口号，它优先级顺序是properties > yml > yaml

> yaml语法

- 大小写敏感
- 冒号后面必须有空格，数量不限

```yaml
server:
	port: 8080
	address: 127.0.0.1
```

> yaml数据格式

- 对象：键值对

  ~~~yaml
  person:
  	name: zhangsan
  person: {name: zhangsan}
  ~~~

- 数组

  ```yaml
  address:
  	- beijing
  	- shanghai
  address: [beijing,shanghai]
  ```

- 纯量（常量）

  ```yml
  msg1: 'hello \n word' #单引号忽略转义字符
  msg2: "hello \n word" #双引号识别转义字符
  ```

参数的引用

~~~yaml
name: zhangsan
person:
	name: ${name}
	age: 23
~~~

> 读取配置文件

- @Value("${key}")

- Environment获取（springframework包下的）

  ~~~java
  @Autowired
  private Environment env;
  @RequestMapping("/hello")
  public String getS() {
      return env.getProperty("name");
  }
  ~~~

- @ConfigurationProperties

  可以加在实体类上

  prefix属性：必要的，值为配置文件中最外面的标签

  ```yaml
  person:
    name: lisi
    age: 14
  ```

  ~~~java
  @Component
  @ConfigurationProperties(prefix = "person")
  public class Person {
      private String name;
      private int age;
  	//getter
      //setter
      //toString
  }
  ~~~

  ```java
  @RestController
  public class TestController {
      @Autowired
      private Person person;
  
      @RequestMapping("/hello")
      public String getS() {
          System.out.println(person);
          return "";
      }
  }
  ```

> profile动态配置切换

1. profile配置方式

   - 多profile文件方式

     现在有三个文件（可以是properties、可以是yml等）：

     - application-dev.properties：开发环境
     - application-pro.properties：生产环境
     - application-test.properties：测试环境

     在application.properties或者其它后缀文件中

     ```properties
     # 这里的pro要和上面定义的后缀一致
     spring.profiles.active=pro
     ```

   - yml多文档方式

     使用---来分开，并且每个都起个名字，在最后定义激活的配置文件名

     ~~~yaml
     ---
     server:
       port: 8081
     profiles: dev
     ---
     server:
       port: 8082
     
     profiles: test
     ---
     spring:
       profiles:
         active: test
     ~~~

     

2. profile激活方式

   - 配置文件
   - 虚拟机参数：-Dspring.profiles.active=dev
   - 命令行参数：java -jar xxx.jar --spring.profiles.active=dev

> 配置加载顺序

- 从高到低

1. file:./config/：当前目录下的config目录下
2. file:./：当前项目根目录
3. classpath:/config/：classpath的/config目录（resource的config文件夹下）
4. classpath:/：classpath的根目录（resource下）

- 使用外部配置文件

  - 通过命令行方式

    --spring.config.location=e://...

> SpringBoot整合其它框架

1. 整合Junit

   添加测试相关注解：

   @RunWith(SpringRunner.class)

   @SpringBootTest(classes=启动类.class)

   如果测试类所在的包是当前要测试的包下或者子包下，classes参数可以不设置，如果不是则要设置

2. 整合Redis

   创建模块选择NoSql选中Redis

   RedisTemplate有默认的配置项是本机，如果不进行配置就是本机，可以用@Autowired配置

   如果想要用其它的地址，可以在yml文件中配置

3. 整合Mybatis

   - 搭建SpringBoot工程
   - 引入Mybatis依赖，添加mysql驱动
   - 编写DataSource和Mybatis配置
   - 定义表和实体类
   - 编写dao和mapper文件
   - 测试

   点击sql中的MyBatisFramework和MysqlDriver创建完成

   > 使用注解配置

   Mapper接口

   ```java
   @Mapper
   public interface UserMapper {
       @Select("select * from t_user2")
       List<User> findAll();
   }
   ```

   application.yml配置文件

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql:///mybatis_study?serverTimezone=UTC
       driver-class-name: com.mysql.cj.jdbc.Driver
       username: root
       password: liujian
   ```

   测试类

   ```java
   @SpringBootTest
   class SpringbootMybatisApplicationTests {
   
       @Autowired
       private UserMapper userMapper;
       @Test
       void testSelectAll() {
           List<User> all = userMapper.findAll();
           System.out.println(all);
       }
   }
   ```

   > 使用xml配置

   略

> SpringBoot常用注解

- @SpringBootApplication

  包含@Configuration、@EnableAutoConfiguration、@ComponentScan，通常用在主类上

- @RestController

  用于控制层，包含了@Controller和@ResponseBody

- @ResponseBody

  表示该方法的返回值直接写入HTTP response body中

- @EnableAutoConfiguration

  让 Spring Boot 根据应用所声明的依赖来对 Spring 框架进行自动配置，一般加在主类上。

- @Profiles

  让这些配置只能在特定的环境下生效

- @ConfigurationProperties

  可以获取配置文件中的属性定义绑到属性中。

- @RequestParam

  用于获取url中?后面的参数

- @RequestBody

  用于获取请求体

> SpringBoot原理分析

1. 自动配置

   通过Conditional注解决定是否配置这个Bean，这个注解需要传入一个Condition实现类，根据逻辑判断返回true或false

   - Spring自定义常用的注解

     - ConditionalOnProperty：判断配置文件中是否有对应属性和值才初始化Bean
     - ConditionalOnClass：判断环境中是否有对应字节码文件才初始化Bean
     - ConditionalOnMissingBean：判断环境中没有对应Bean才初始化这个Bean

   - @Enable*开头的

     用于动态启用某些功能，实现Bean的动态加载，底层是用@Import注解实现的

   - @Import提供的四种用法

     - 导入Bean
     - 导入配置类
     - 导入ImportSelector实现类。一般用于加载配置文件中的类
     - 导入ImportBeanDefinitionRegister实现类

   - @EnableAutoConfiguration

     - 自定义starter

2. 监听机制

   实现几个接口：

   - ApplicationContextInitializer
   - SpringApplicationRunListener
   - CommandLineRunner
   - ApplicationRunner

   SpringApplicationRunListener和CommandLineRunner实现类会在spring启动的时候就初始化完成。

   另外两个需要在配置文件中配一下才会初始化，在resources下的META-INFO下的spring.factories中配置，把两个接口的全限定名设置等于自己实现的对应的类的全限定类名，这样的一个键值对的形式。

3. 启动流程分析

   - SpringBoot启动的时候，会构造一个SpringApplication的实例，构造SpringApplication的时候会进行初始化的工作，初始化的时候会做以下几件事：
     1. 把参数sources设置到SpringApplication属性中，这个sources可以是任何类型的参数.
     2. 判断是否是web程序，并设置到webEnvironment的boolean属性中.
     3. 创建并初始化ApplicationInitializer，设置到initializers属性中 。
     4. 创建并初始化ApplicationListener，设置到listeners属性中 。
     5. 初始化主类mainApplicatioClass。

   - SpringApplication构造完成之后调用run方法，启动SpringApplication，run方法执行的时候会做以下几件事：
     1. 构造一个StopWatch计时器，用来记录SpringBoot的启动时间 。
     2. 初始化监听器，获取SpringApplicationRunListeners并启动监听，用于监听run方法的执行。
     3. 创建并初始化ApplicationArguments,获取run方法传递的args参数。
     4. 创建并初始化ConfigurableEnvironment（环境配置）。封装main方法的参数，初始化参数，写入到 Environment中，发布ApplicationEnvironmentPreparedEvent（环境事件），做一些绑定后返回Environment。
     5. 打印banner和版本。
     6. 构造Spring容器(ApplicationContext)上下文。先填充Environment环境和设置的参数，如果application有设置beanNameGenerator（bean）、resourceLoader（加载器）就将其注入到上下文中。调用初始化的切面，发布ApplicationContextInitializedEvent（上下文初始化）事件。
     7. SpringApplicationRunListeners发布finish事件。
     8. StopWatch计时器停止计时，日志打印总共启动的时间。
     9. 发布SpringBoot程序已启动事件(started())
     10. 调用ApplicationRunner和CommandLineRunner
     11. 最后发布就绪事件ApplicationReadyEvent，标志着SpringBoot可以处理就收的请求了(running())