# SpringBoot2

优点

- 创建独立Spring应用
- 内嵌Web应用
- 自动starter依赖
- 自动配置spring以及第三方功能
- 提供生产级别的监控、健康检查
- 无需编写xml

## 微服务

## 分布式

分布式的困难

1. 远程调用
2. 服务发现
3. 负载均衡
4. 服务容错
5. 配置管理
6. 服务监控
7. 链路追踪
8. 日志管理
9. 任务调度
10. ...

## 云原生

上云的困难

1. 服务自愈
2. 弹性伸缩
3. 服务隔离
4. 自动化部署
5. 灰度发布
6. 流量治理
7. ...

## SpringBoot

官方文档：https://docs.spring.io/spring-boot/docs/current/reference/html/index.html

构建步骤：

构建maven

引入依赖

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.1</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

创建主程序

- 固定写法

```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

编写业务

测试

简化配置

application.yml

简化部署

- 使用springboot自带插件，会自动打成jar包，可以直接执行

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

### 依赖管理

- 自动版本仲裁机制：

  依赖的版本号可以不用写，如果在父类中有的话。

- 修改父类中的版本

  ```xml
  使用properties属性覆盖(要先去父类中看一下具体的key，再根据key修改)
  <properties>
      <mysql.version>8.0.21</mysql.version>
      <java.version>11</java.version>
  </properties>
  ```

### 自动配置

- 自动配置好Tomcat

- 自动配置好SpringMVC

- 默认的包结构

  `启动类所在的包以及其子包会自动扫描，如果在启动类外面，那么就不会扫描，所以要注意启动类的位置`

  如果想要改变包扫描的路径，可以在启动类的注解后面添加@SpringBootApplication(scanBasePackages = "com.van.demo")，或者ComponentScan注解，但是这个注解不能重复，所以可以去看@SpringBootApplication注解的父注解，把他们拿出来替换掉SpringBootApplication注解，其中有一个就是包扫描。

- 按需加载自动配置（@Conditional）

### 容器

- @Configuration注解

  - 告诉springboot这是一个配置类，同时这个配置类本身也是一个JavaBean，会被配置到容器中

  - proxyBeanMethods属性：Full和lite模式
    - 值为true：会用代理对象调用这个里面的方法，SpringBoot总会检查这个javaBean是否在容器中有。保持组件单实例
    - 值为false：在外面每一次调用都会产生一个新的对象
    - 解决的问题：JavaBean的依赖

- @Import注解

  可以导入其它组件

- @Conditional一系列注解

  在SpringBoot1中写过

- @ImportResource

  可以导入其它xml配置文件，让其生效

### 配置绑定

- 只有在容器中的组件才能拥有springboot的功能

- 方法一：

  使用@Component和@ConfigurationProperties一起使用

  可以加载yml文件中配置好的组件，使用prefix属性添加前缀

  ```yml
  person:
    name: lisi
    age: 24
  ```

  ```java
  @Component
  @ConfigurationProperties(prefix = "person")
  public class Person {
      private String name;
      private int age;
  
      public String getName() {
          return name;
      }
  
      public void setName(String name) {
          this.name = name;
      }
  
      public int getAge() {
          return age;
      }
  
      public void setAge(int age) {
          this.age = age;
      }
  
      @Override
      public String toString() {
          return "Person{" +
                  "name='" + name + '\'' +
                  ", age=" + age +
                  '}';
      }
  }
  ```

  ```java
  @RestController
  public class TestController {
  
      @Autowired
      private Person person;
  
      @RequestMapping("/hello")
      public String getS() {
          return person.toString();
      }
  }
  ```

  

- 方法二：

  在配置类上加@EnableConfigurationProperties(Person.class)注解，以及需要注入的类上加@ConfigurationProperties(prefix = "person")

  这种情况适用场景比如，使用第三方jar包的时候不可以给别人的类上加@Component注解，所以使用这个方式解决

  ```yaml
  person:
    name: zhangsan
    age: 23
  ```

  ```java
  @Configuration
  @EnableConfigurationProperties(Person.class)
  //开启Person配置绑定，把组件自动注册到容器中
  public class MyConfig {
  
  }
  ```

  ```java
  @ConfigurationProperties(prefix = "person")
  public class Person {
      private String name;
      private int age;
  
      public Person() {
      }
  
      public Person(String name, int age) {
          this.name = name;
          this.age = age;
      }
  
      public String getName() {
          return name;
      }
  
      public void setName(String name) {
          this.name = name;
      }
  
      public int getAge() {
          return age;
      }
  
      public void setAge(int age) {
          this.age = age;
      }
  
      @Override
      public String toString() {
          return "Person{" +
                  "name='" + name + '\'' +
                  ", age=" + age +
                  '}';
      }
  }
  ```

  ```java
  @RestController
  public class TestController {
  
      @Autowired
      private Person person;
  
      @RequestMapping("/hello")
      public String getS() {
          return person.toString();
      }
  }
  ```

 ### 自动配置原理

@SpringBootApplication

```java
@SpringBootConfiguration  //表明这是一个配置类
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {}
```

@EnableAutoConfiguration

```java
@AutoConfigurationPackage
@Import({AutoConfigurationImportSelector.class})
public @interface EnableAutoConfiguration {}
```

@AutoConfigurationPackage

```java
@Import({Registrar.class})  //给容器中导入一个组件
public @interface AutoConfigurationPackage {}
//利用Register给容器中导入一系列组件
//将指定包下所有的组件导入进来
```

@Import({AutoConfigurationImportSelector.class})

...

> 按需开启自动配置

- SpringBoot会先加载所有的自动配置类  XXXAutoConfiguration
- 配置类按照条件进行生效，默认都会绑定配置文件指定的值。
- 生效的配置类就会给容器中装配很多的组件

> 最佳实践

- 引入场景依赖

- 查看自己配置了哪些

  在配置文件中使用debug=true

- 是否需要修改

### 配置文件

1. yml文件

   基本数据类型、实体类型、数组、集合



### 简单功能分析

1. 静态资源访问

   - 静态资源目录

     类路径下：/static（or /public or /resources or /META-INF/resources）

   - 原理：静态映射/**

     请求进来先去controller中看能不能处理，不能处理的请求都交给静态资源处理器，去上面的目录中查找

   - 静态资源访问前缀

     （默认无前缀）

     sping.mvc.static-path-pattern: /resources/**(这个resources就是自定义的静态资源前缀)

   - 静态资源路径：设置静态资源只能上这里查找

     spring.resources.static-locations: classpath:/temp(temp是自定义路径)

   - webjar

     自动映射

     https://www.webjars.org/

     可以直接添加jQuery这样的包的依赖

   - 欢迎页支持

     1. 在默认的静态路径下设置index.html可以默认打开

### 请求参数处理

Rest原理（只针对表单提交要使用Rest的时候）

- 如果要发PUT/DELETE请求，方式都设置为post。并且添加一个隐藏项，_method=PUT/DELETE。

- 显示开启

  ```yaml
  spring:
    mvc:
      hiddenmethod:
        filter:
          enabled: true
  ```

- 请求过来被HiddenHttpMethodFilter拦截

  - 请求是否正常，并且是post
  - 获取_method的值

- 同时可以直接使用@GetMapping/@PostMapping/@PutMapping/@DeleteMapping

代码样例：

```html
<form action="/test" method="post">
    <input name="_method" type="hidden" value="put">
    <input type="submit" value="rest-put">
</form>
```

```yaml
spring:
  mvc:
    hiddenmethod:
      filter:
        enabled: true
```

```java
@PutMapping("/test")
public String put() {
    return "put";
}
```



### 请求映射原理

所有的请求都映射在HandlerMapping中

springboot自动配置了一些HandlerMapping

- RequestMappingHandlerMapping
- WelcomePageHandlerMapping
- ...

如果需要自定义映射处理，可以自己添加一些HandlerMapping

### 常用注解

@PathVariable

@RequestHeader

@RequestParam

@CookieValue

@RequestBody

@RequestAttribute

@MatrixVariable

注解样例：

```java
@RequestMapping("/test1/{username}/{password}")
public String test1(@PathVariable("username") String username,@PathVariable("password") String password) {
    return username + "==>" + password;
}

@RequestMapping("/test2/{username}/{password}")
public Map<String,String> test2(@PathVariable Map<String,String> map) {
    return map;
}

@RequestMapping("/test3")
public Map<String, String> test3(@RequestHeader("User-agent") String userAgent, @RequestHeader Map<String,String> maps) {
    maps.put("useragent2",userAgent);
    return maps;
}

@RequestMapping("/test4")
public Map<String,String> test4(@RequestParam("username") String username,@RequestParam("password") String password,@RequestParam Map<String,String> map) {
    map.put("username2",username);
    map.put("password2",password);
    return map;
}
@RequestMapping("/test5")
public void test5(@CookieValue("Idea-3e442316") String cookieOne, @CookieValue("Idea-3e442316") Cookie cookie) {
    System.out.println(cookieOne);
    System.out.println(cookie.toString());
}

@PostMapping("/test6")
public Map test6(@RequestBody String body) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("body",body);
    return map;
}
<form action="/test6" method="post">
    <input type="text" name="username">
    <input type="text" name="password">
    <input type="submit">
</form>
```

@RequestAttribute：获取request域中的数据（当然，我们也可以直接从request中取）

```java
@Controller
public class RequestController {

    @RequestMapping("/goto")
    public String goTo(HttpServletRequest request) {
        request.setAttribute("msg1","msg1set成功");
        request.setAttribute("msg2",22);
        return "forward:/success";
    }

    @ResponseBody
    @RequestMapping("/success")
    public Map success(@RequestAttribute("msg1") String msg1,
                       @RequestAttribute("msg2") Integer msg2,
                       HttpServletRequest request) {
        Object msg11 = request.getAttribute("msg1");
        Object msg21 = request.getAttribute("msg2");
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg1",msg1);
        map.put("msg2",msg2);
        map.put("msg11",msg11);
        map.put("msg21",msg21);
        return map;
    }
}
```



@MatrixVariable

- 矩阵变量：
  - 矩阵变量应该绑定在路径变量中
  - 每一个矩阵变量都用;隔开。多个值可以用逗号隔开。

- 后端接收矩阵变量
  - 使用@MatrixVariable注解

```java
//SpringBoot默认禁用矩阵变量功能,需要手动开启
//手动开启: UrlPathHelper
//两种方案:
//1.实现WebMvcConfigure,重写configurePathMatch方法,设置UrlPathHelper
@RequestMapping("/test7/{path}")
public String test7(@MatrixVariable("username") String username,@MatrixVariable("password") String password) {
    return username+"=>"+password;
}
```

```java
@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        //不移除分号后面的内容.设置为false后,矩阵变量功能可以生效
        urlPathHelper.setRemoveSemicolonContent(false);
        configurer.setUrlPathHelper(urlPathHelper);

    }
}
```

也可以添加为一个JavaBean

```java
@Configuration(proxyBeanMethods = false)
public class WebConfig {
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                UrlPathHelper urlPathHelper = new UrlPathHelper();
                urlPathHelper.setRemoveSemicolonContent(false);
                configurer.setUrlPathHelper(urlPathHelper);
            }
        };
    }
}
```

使用url访问：http://localhost:8080/test7/cell;username=zhasan;password=123。这里的cell随便写。



### 拦截器

- 拦截器需要实现HandlerInterceptor接口，重写里面的三个方法
  - preHandle
  - postHandle
  - afterCompletion
- 在配置类中定义好拦截器，需要指定拦截的路径以及资源和放行的路径以及资源

LoginInterceptor：登录拦截器

```java
/**
 * 登录检查
 * 1. 配置好拦截器要拦截哪些请求
 * 2. 将这些配置放在容器中
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        System.out.println("拦截的路径："+requestURI);

        HttpSession session = request.getSession();
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser != null) {
            //放行
            return true;
        } else {
            //未登录，跳转到登录页面
            request.setAttribute("msg","请先登录");
            request.getRequestDispatcher("/").forward(request,response);
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("postHandle execute"+modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("afterCompletion execute"+ex);
    }
}
```

AdminWebConfig：配置要拦截以及放行的资源或路径

```java
@Configuration
public class AdminWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                //拦截所有请求和资源（包括静态资源
                .addPathPatterns("/**")
                //放行的路径和资源
               .excludePathPatterns("/","/login","/css/**","/fonts/**","/images/**","/js/**")        
    }
}
```

### 文件上传功能

SpringBoot对于文件上传有一个默认配置，在MultipartConfiguration中

```java
@Controller
public class FormController {

    @GetMapping("/form_layouts")
    public String form_layouts() {
        return "form/form_layouts";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("email") String email,
                         @RequestParam("username") String username,
                         @RequestPart("headerImage")MultipartFile headerImage,
                         @RequestPart("photos") MultipartFile[] photos) throws IOException {

        System.out.println("email: "+email+"\nusername: "+username);
        System.out.println("headerImageName2: "+headerImage.getOriginalFilename());
        System.out.println("photos.length: "+photos.length);

        //可以将文件上传到文件服务器，这里直接保存到本机磁盘
        if (!headerImage.isEmpty()) {
            headerImage.transferTo(new File("F:\\"+headerImage.getOriginalFilename()));
        }
        if (photos.length > 0) {
            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    photo.transferTo(new File("F:\\"+photo.getOriginalFilename()));
                }
            }
        }

        return "main";
    }
}
```

```java
@AutoConfiguration
@ConditionalOnClass({ Servlet.class, StandardServletMultipartResolver.class, MultipartConfigElement.class })
@ConditionalOnProperty(prefix = "spring.servlet.multipart", name = "enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(MultipartProperties.class)
public class MultipartAutoConfiguration {}
```

其中的@EnableConfigurationProperties注解绑定了配置文件,其中的属性可以在application.yml中修改

```java
@ConfigurationProperties(prefix = "spring.servlet.multipart", ignoreUnknownFields = false)
public class MultipartProperties {

   /**
    * Whether to enable support of multipart uploads.
    */
   private boolean enabled = true;

   /**
    * Intermediate location of uploaded files.
    */
   private String location;

   /**
    * Max file size.
    */
   private DataSize maxFileSize = DataSize.ofMegabytes(1);

   /**
    * Max request size.
    */
   private DataSize maxRequestSize = DataSize.ofMegabytes(10);

   /**
    * Threshold after which files are written to disk.
    */
   private DataSize fileSizeThreshold = DataSize.ofBytes(0);
}
```

修改默认的上传文件大小，或者其它参数

```yaml
spring:
  servlet:
    multipart:
	  #单个文件的最大大小
      max-file-size: 10MB
	  #上传的整个请求的文件大小最大
      max-request-size: 100MB
```



### 异常处理

SpringBoot默认异常处理在static或者templates文件夹下的error文件夹，这个文件夹里面的页面会自动被浏览器使用，404或者5xx等。



### Web原生组件注入（Servlet、Filter、Listener）

#### 使用ServletAPI

- Servlet

```java
@WebServlet(urlPatterns = "/myServlet")
public class MyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("hello");
    }
}
```

```java
//启动类中需要添加包扫描Servlet
@ServletComponentScan("com.van.admin")
@SpringBootApplication
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

使用Servlet访问资源并不会走Spring的拦截器interceptor。这是为什么

- Filter

```java
@WebFilter(urlPatterns = {"/css/*","/images/*"})
public class MyFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("初始化完成");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("filter执行过滤");
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {
        System.out.println("filter销毁");
    }
}
```

Listener

```java
@WebListener
public class MyListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("监听器初始化");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("监听器销毁");
    }
}
```

#### 使用RegistrationBean

ServletRegistrationBean、FilterRegistrationBean、ServletListenerRegistrationBean

使用这种方式的时候，@WebServlet、@WebFilter、@WebListener注解不加

```java
@Configuration(proxyBeanMethods = true)
public class MyRegisterConfig {

    @Bean
    public ServletRegistrationBean myServlet() {
        MyServlet myServlet = new MyServlet();
        return new ServletRegistrationBean(myServlet, "/myServlet", "/my");
    }

    @Bean
    public FilterRegistrationBean myFilter() {
        MyFilter myFilter = new MyFilter();
        //两种方式
        //1. 调用这个配置类中的方法直接返回
        // return new FilterRegistrationBean(myFilter,myServlet());
        //2. 新建一个FilterRegistrationBean，传入过滤器，设置拦截路径，再返回
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(myFilter);
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/myServlet", "/css/*"));
        return filterRegistrationBean;
    }

    @Bean
    public ServletListenerRegistrationBean myListener() {
        MyListener myListener = new MyListener();
        return new ServletListenerRegistrationBean(myListener);
    }
}
```



### 定制化原理

- 常见的定制化方式：
  - 修改配置文件
  - xxxxxxCustomizer
  - 编写自定义配置类 xxxConfiguration; + Bean替换、增加容器中默认组件；视图解析器
  - web应配置类实现WebMvcConfigurer + Bean即可定制化web功能
  - @EnableWebMvc + WebMvcConfigure可以全面接管springmvc，所有规则要自己重新配置；
  - ...

### 数据访问

#### MySql

1. 导入JDBC场景

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jdbc</artifactId>
   </dependency>
   ```

   - 父类导入了一些其它依赖

   ```xml
   <!--数据库连接池-->
   <dependency>
       <groupId>com.zaxxer</groupId>
       <artifactId>HikariCP</artifactId>
       <version>4.0.3</version>
       <scope>compile</scope>
   </dependency>
   ```

   但是不会帮我们导入驱动类，为什么不导入驱动类？

   根据数据库的版本不同可能导入的驱动有些变化，所以要自己导入

   ```xml
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
   </dependency>
   ```

2. 分析自动配置

   org.springframework.boot.autoconfigure.jdbc;

   有很多关于数据库的自动配置

   - DataSourceAutoConfiguration：数据源的自动配置
   - DataSourceTransactionManagerAutoConfiguration：事务管理
   - JdbcTemplateAutoConfiguration：JdbcTemplate的自动配置，可以对数据库crud
   - JndiDataSourceAutoConfiguration：jndi的自动配置
   - XADataSourceAutoConfiguration：分布式事务相关的

3. 修改配置

   ```yaml
   spring:
     datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://localhost:3306/mybatis_study?serverTimezone=UTC&useSSL=false
       username: root
       password: 
     jdbc:
       template:
         query-timeout: 3
   ```

4. Druid数据源starter整合

   导入依赖：

   ```xml
   <dependency>
       <groupId>com.alibaba</groupId>
       <artifactId>druid-spring-boot-starter</artifactId>
       <version>1.1.7</version>
   </dependency>
   ```

5. **整合Mybatis**

   导入依赖

   ```xml
   <dependency>
       <groupId>org.mybatis.spring.boot</groupId>
       <artifactId>mybatis-spring-boot-starter</artifactId>
       <version>2.2.2</version>
   </dependency>
   ```

   配置模式

   - 全局配置文件
   - SqlSessionFactory：自动配置好了
   - SqlSession：自动配置了SqlSessionTemplate，组合了SqlSession
   - Mapper：写了@Mapper注解就会被自动扫描

   > 方式一：配置文件

   mybatis-config.xml、mapper/*.xml、mapper/....Mapper

   在yml文件中配置

   ```yaml
   mybatis:
     config-location: classpath:mybatis/mybatis-config.xml  # 全局配置文件的位置
     mapper-locations: classpath:mybatis/mapper/*.xml # mapper映射文件位置
   # 再让xml绑定mapper接口
   ```

   > 方式二：注解
   
   ```java
   @org.springframework.context.annotation.Configuration
   @ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
   @ConditionalOnSingleCandidate(DataSource.class)
   @EnableConfigurationProperties(MybatisProperties.class)
   @AutoConfigureAfter({ DataSourceAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class })
   public class MybatisAutoConfiguration implements InitializingBean {}
   ```
   
   这个@EnableConfigurationProperties绑定了MybatisProperties文件，这个文件中配置了默认的一些属性，想要在yml中修改可以来这看修改哪些项。
   
   ```java
   @ConfigurationProperties(prefix = "mybatis")
   public class MybatisProperties {}
   ```
   
   全局配置要么都写在yml中，要么写在xml中，不能同时存在，会报错，就像下面的驼峰命名就是全局配置。
   
   ```yaml
   mybatis:
     configuration:
       map-underscore-to-camel-case: true #开启驼峰命名
   ```
   
   在mapper接口中直接写方法
   
   ```java
   @Mapper
   public interface UserMapper {
       @Select("select * from t_user2 where id = #{id}")
       User2 getUserById(int id);
   }
   ```
   
   有时候插入语句需要返回一个封装好主键id的对象，xml中使用useGeneratedKeys，这里可以使用@Options注解，@Options注解里还有一些其它属性。
   
   ```java
   @Insert("insert into t_user2(username,password) values(#{username},#{password})")
   @Options(useGeneratedKeys = true,keyProperty = "id")
   void insertUser(User2 user);
   ```
   
   `注解方式对于写复杂的sql语句不太适合，适合写简单的sql语句。`
   
   可以使用xml和注解混合使用。（但是他们的全局配置只能在一个地方配置）
   
6. 整合Mybatis-Plus完成CRUD

   导入依赖

   ```xml
   <dependency>
       <groupId>com.baomidou</groupId>
       <artifactId>mybatis-plus-boot-starter</artifactId>
       <version>3.5.2</version>
   </dependency>
   ```

   这个plus的依赖里包含了mybatis核心包，mybatis整合spring包，mybatis整合jdbc包，如果导入了plus，那么下面这两个依赖就不用再导入了

   ```xml
   <dependency>
       <groupId>org.mybatis.spring.boot</groupId>
       <artifactId>mybatis-spring-boot-starter</artifactId>
       <version>2.2.2</version>
   </dependency>
   
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jdbc</artifactId>
   </dependency>
   ```

   mapper可以继承BaseMapper，但是对于表名，默认是实体类的类名首字母小写，可以使用@TableName注解在实体类中指定数据库中的表名

   ```java
   /**
    * 只需要继承BaseMapper，就可以拥有简单的crud功能
    * 如果想要复杂的功能，可以自己写一个xml的mapper映射文件
    */
   @Mapper
   public interface User3Mapper extends BaseMapper<User3> {
   
   }
   ```

   ```java
   import com.baomidou.mybatisplus.annotation.TableName;
   @TableName("t_user3")
   public class User3 {
       private Integer id;
       private String name;
       private Integer age;
       private String email;
       //这个注解表示在数据库中这个字段不存在，不用查找
       @TableField(exist = false)
       private String test;
       //constructor,getter,setter,toString
   }
   ```

   - IService接口是所有Service的顶级父类，Service接口可以继承它
   - service继承IService后，service的实现类要实现很多来自IService的方法，我们可以让实现类继承ServiceImpl类，传入两个泛型参数，第一个表示要使用的Mapper接口，第二个参数是要操作的类型。
   - 这样之后，我们可以在controller中注入User3Service，并直接调用自带的一些方法

   ```java
   public interface User3Service extends IService<User3> {
   }
   ```

   ```java
   @Service
   public class User3ServiceImpl extends ServiceImpl<User3Mapper, User3> implements User3Service {
   }
   ```

   实现删除功能

   - 每次删除的时候前端点击删除，要传递给后端的参数有
     - 当前要删除的数据的id
     - 当前页码：用于后端删除后跳转的路径
   - 后端删除后，如果想要跳转路径的时候携带参数，可以使用RedirectAttributes对象，addAttribute方法把参数传递进去，再通过redirect重定向

   ```java
   @RequestMapping("/user/delete/{id}")
   public String deleteUser(@PathVariable("id") Integer id,
                            @RequestParam(value = "pn",defaultValue = "1") Integer pn,
                            RedirectAttributes redirectAttributes) {
       user3Service.removeById(id);
       redirectAttributes.addAttribute("pn",pn);
       return "redirect:/dynamic_table";
   }
   ```

   ```html
   <!--删除按钮-->
   <td>
       <a  th:href="@{/user/delete/{id}(id=${user.id},pn=${page.current})}" class="btn btn-danger btn-sm" type="button">删除</a>
   </td>
   ```

> 补充

分页操作

下面这个User3Service继承了IService，这些分页的方法都来自于IService以及它的实现类。上面所说过的ServiceImpl

```java
/**
 *
 * @param pn 传入当前页码
 * @param model
 * @return
 */
@GetMapping("/dynamic_table")
public String dynamic_table(@RequestParam(value = "pn",defaultValue = "1") Integer pn, Model model) {
    List<User3> list = user3Service.list();
    model.addAttribute("users",list);

    //Page对象第一个参数表示当前页面，第二个参数表示当前页显示几条数据
    Page<User3> user3Page = new Page<>(pn, 2);
    //返回的page是分页查询的结果
    Page<User3> page = user3Service.page(user3Page);

    //当前页码
    //page.getCurrent();
    //总页码
    //page.getPages();
    //查询出的所有记录
    //page.getRecords();
    //每页显示条数
    //page.getSize();
    //总记录数
    //page.getTotal();
    //......

    model.addAttribute("page",page);
    return "table/dynamic_table";
}
```

需要联合一个插件，MybatisPlusInterceptor，这个插件可以让分页显示出来

```java
@Configuration
public class MyBatisConfig {

    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        //这是分页拦截器
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        //点击下一页到头后，再次点击是跳回到首页还是继续查询
        paginationInnerInterceptor.setOverflow(true);
        //每页限制多少条记录，-1L表示不受限制
        paginationInnerInterceptor.setMaxLimit(-1l);
        mybatisPlusInterceptor.addInnerInterceptor(paginationInnerInterceptor);
        return mybatisPlusInterceptor;
    }
}
```

```html
<table class="display table table-bordered table-striped" id="dynamic-table">
    <thead>
    <tr>
        <th>#</th>
        <th>id</th>
        <th>name</th>
        <th>age</th>
        <th>email</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
    <tr class="gradeX" th:each="user,stat:${page.records}">
        <td th:text="${stat.count}"></td>
        <td th:text="${user.id}">id</td>
        <td th:text="${user.name}"></td>
        <td th:text="${user.age}">Win 95+</td>
        <td class="center hidden-phone">[[${user.email}]]</td>
        <td class="center hidden-phone">X</td>
    </tr>

    </tbody>
</table>
<div class="row-fluid">
    <div class="span6">
        <div class="dataTables_info" id="dynamic-table_info">当前第 [[${page.current}]] 页
            总计 [[${page.pages}]] 页
            共 [[${page.total}]] 条记录
        </div>
    </div>
    <div class="span6">
        <div class="dataTables_paginate paging_bootstrap pagination">
            <ul>
                <li class="prev disabled"><a href="#">← Previous</a></li>
                <!--设置哪个页标高亮，页标的个数，要跳转的地址-->
                <li th:class="${num==page.current?'active':''}" th:each="num:${#numbers.sequence(1,page.pages)}">
                    <a th:href="@{/dynamic_table(pn=${num})}">[[${num}]]</a>
                </li>
                <li class="next disabled"><a href="#">Next → </a></li>
            </ul>
        </div>
    </div>
</div>
```

#### NoSql

1. Redis配置

   - 导入依赖

     ```xml
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-data-redis</artifactId>
     </dependency>
     ```

   - 自动配置

     RedisAutoConfiguration自动配置类。RedisProperties属性->spring.redis.xxx对redis的配置

     连接工厂。LettuceConnectionConfiguration、JedisConnectionConfiguration

     自动注入了RedisTemplate<Object,Object>

     自动注入了SpringRedisTemplate；k，v都是String

     ```java
     @AutoConfiguration
     @ConditionalOnClass(RedisOperations.class)
     @EnableConfigurationProperties(RedisProperties.class)
     @Import({ LettuceConnectionConfiguration.class, JedisConnectionConfiguration.class })
     public class RedisAutoConfiguration {}
     ```

   - yml配置

     ```java
     /**
      * Connection URL. Overrides host, port, and password. User is ignored. Example:
      * redis://user:password@example.com:6379
      */
     private String url;
     可以只配置一个url，或者分开配置
     ```

     ```yaml
     redis:
     #url:
     username:
     password:
     port:
     host:
     ```

   - 操作redis

     ```java
     @Test
     void testRedis() {
         ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
         operations.set("hello","word");
         System.out.println(operations.get("hello"));
     }
     ```

   - 上述是使用RedisTemplate与Lettuce（RedisTemplate底层就是Lettuce）

   - 下面底层使用Jedis

     导入jedis依赖

     ```xml
     <dependency>
         <groupId>redis.clients</groupId>
         <artifactId>jedis</artifactId>
     </dependency>
     ```

     yml配置

     ```yaml
     redis:
     #url:
     #username:
     #password:
     port: 6379
     host: localhost
     client-type: jedis
     ```

     ```java
     @Test
     void testRedis() {
         //该怎么操作还是怎么操作，只是yml配置的时候添加一个client-type
         ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
         operations.set("hello","word");
         System.out.println(operations.get("hello"));
         //输出看一下底层用的是哪一个连接Jedis/Lettuce
         RedisConnection connection = redisConnectionFactory.getConnection();
         System.out.println(connection.getClass());
     }
     ```

     > Filter和Interceptor几乎有同种功能？

     1. Filter是Servlet定义的原生组件。好处脱离spring应用也能使用
     2. Interceptor是Spring定义的接口，可以使用Spring注解

### 单元测试

#### Junit5的变化

Junit Platform + Junit Jupiter + Junit Vintage

SpringBoot2.4以上版本移除了对Vintage的依赖。如果需要兼容Junit4需要自行引入

```xml
<dependency>
    <groupId>org.junit.vintage</groupId>
    <artifactId>junit-vintage-engine</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.hamcrest</groupId>
            <artifactId>hamcrest-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

以前：@SpringBootTest+@RunWith(SpringTest.class)

现版本使用

```java
@SpringBootTest
class AdminApplicationTests {

    @Test
    void testRedis() {

    }
}
```

#### Junit5常用注解

官网：https://junit.org/junit5/docs/current/user-guide/#writing-tests-annotations

| Annotation               | Description                                                  |
| :----------------------- | :----------------------------------------------------------- |
| `@Test`                  | Denotes that a method is a test method. Unlike JUnit 4’s `@Test` annotation, this annotation does not declare any attributes, since test extensions in JUnit Jupiter operate based on their own dedicated annotations. Such methods are *inherited* unless they are *overridden*. |
| `@ParameterizedTest`     | Denotes that a method is a [parameterized test](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests). Such methods are *inherited* unless they are *overridden*. |
| `@RepeatedTest`          | Denotes that a method is a test template for a [repeated test](https://junit.org/junit5/docs/current/user-guide/#writing-tests-repeated-tests). Such methods are *inherited* unless they are *overridden*. |
| `@TestFactory`           | Denotes that a method is a test factory for [dynamic tests](https://junit.org/junit5/docs/current/user-guide/#writing-tests-dynamic-tests). Such methods are *inherited* unless they are *overridden*. |
| `@TestTemplate`          | Denotes that a method is a [template for test cases](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-templates) designed to be invoked multiple times depending on the number of invocation contexts returned by the registered [providers](https://junit.org/junit5/docs/current/user-guide/#extensions-test-templates). Such methods are *inherited* unless they are *overridden*. |
| `@TestClassOrder`        | Used to configure the [test class execution order](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-execution-order-classes) for `@Nested` test classes in the annotated test class. Such annotations are *inherited*. |
| `@TestMethodOrder`       | Used to configure the [test method execution order](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-execution-order-methods) for the annotated test class; similar to JUnit 4’s `@FixMethodOrder`. Such annotations are *inherited*. |
| `@TestInstance`          | Used to configure the [test instance lifecycle](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle) for the annotated test class. Such annotations are *inherited*. |
| `@DisplayName`           | Declares a custom [display name](https://junit.org/junit5/docs/current/user-guide/#writing-tests-display-names) for the test class or test method. Such annotations are not *inherited*. |
| `@DisplayNameGeneration` | Declares a custom [display name generator](https://junit.org/junit5/docs/current/user-guide/#writing-tests-display-name-generator) for the test class. Such annotations are *inherited*. |
| `@BeforeEach`            | Denotes that the annotated method should be executed *before* **each** `@Test`, `@RepeatedTest`, `@ParameterizedTest`, or `@TestFactory` method in the current class; analogous to JUnit 4’s `@Before`. Such methods are *inherited* – unless they are *overridden* or *superseded* (i.e., replaced based on signature only, irrespective of Java’s visibility rules). |
| `@AfterEach`             | Denotes that the annotated method should be executed *after* **each** `@Test`, `@RepeatedTest`, `@ParameterizedTest`, or `@TestFactory` method in the current class; analogous to JUnit 4’s `@After`. Such methods are *inherited* – unless they are *overridden* or *superseded* (i.e., replaced based on signature only, irrespective of Java’s visibility rules). |
| `@BeforeAll`             | Denotes that the annotated method should be executed *before* **all** `@Test`, `@RepeatedTest`, `@ParameterizedTest`, and `@TestFactory` methods in the current class; analogous to JUnit 4’s `@BeforeClass`. Such methods are *inherited* – unless they are *hidden*, *overridden*, or *superseded*, (i.e., replaced based on signature only, irrespective of Java’s visibility rules) – and must be `static` unless the "per-class" [test instance lifecycle](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle) is used. |
| `@AfterAll`              | Denotes that the annotated method should be executed *after* **all** `@Test`, `@RepeatedTest`, `@ParameterizedTest`, and `@TestFactory` methods in the current class; analogous to JUnit 4’s `@AfterClass`. Such methods are *inherited* – unless they are *hidden*, *overridden*, or *superseded*, (i.e., replaced based on signature only, irrespective of Java’s visibility rules) – and must be `static` unless the "per-class" [test instance lifecycle](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle) is used. |
| `@Nested`                | Denotes that the annotated class is a non-static [nested test class](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested). On Java 8 through Java 15, `@BeforeAll` and `@AfterAll` methods cannot be used directly in a `@Nested` test class unless the "per-class" [test instance lifecycle](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle) is used. Beginning with Java 16, `@BeforeAll` and `@AfterAll` methods can be declared as `static` in a `@Nested` test class with either test instance lifecycle mode. Such annotations are not *inherited*. |
| `@Tag`                   | Used to declare [tags for filtering tests](https://junit.org/junit5/docs/current/user-guide/#writing-tests-tagging-and-filtering), either at the class or method level; analogous to test groups in TestNG or Categories in JUnit 4. Such annotations are *inherited* at the class level but not at the method level. |
| `@Disabled`              | Used to [disable](https://junit.org/junit5/docs/current/user-guide/#writing-tests-disabling) a test class or test method; analogous to JUnit 4’s `@Ignore`. Such annotations are not *inherited*. |
| `@Timeout`               | Used to fail a test, test factory, test template, or lifecycle method if its execution exceeds a given duration. Such annotations are *inherited*. |
| `@ExtendWith`            | Used to [register extensions declaratively](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-declarative). Such annotations are *inherited*. |
| `@RegisterExtension`     | Used to [register extensions programmatically](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-programmatic) via fields. Such fields are *inherited* unless they are *shadowed*. |
| `@TempDir`               | Used to supply a [temporary directory](https://junit.org/junit5/docs/current/user-guide/#writing-tests-built-in-extensions-TempDirectory) via field injection or parameter injection in a lifecycle method or test method; located in the `org.junit.jupiter.api.io` package. |

@DisplayName：为测试类或测试方法设置展示名称

@BeforeEach：每个测试方法开始前都会执行

@AfterEach：每个测试方法结束后都会执行

@BeforeAll：所有测试开始之前执行一次，这个方法必须是static，或者标注@TestInstance注解，表示测试方法有一个实例

@AfterAll：所有测试结束后执行一次，这个方法必须是static，或者标注@TestInstance注解，表示测试方法有一个实例

@Tag：表示单元测试的类别

@Disabled：表示这个方法不用了，测试的时候不会再测试

@Timeout：设置超时提醒

@ExtendWith：用来扩展功能

@RepeatedTest：表示方法可重复执行

#### 断言

所有的断言都在Assertions类中且都是静态方法：https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html

只要有一个断言未通过，未通过的断言下面的代码就不会执行

```java
@SpringBootTest
public class JunitTest {

    @Test
    void test1() {
        assertEquals(1,1);
        assertNotEquals(1,1);
        //判断两个对象的引用是否指向同一个对象
        assertSame(new Object(),new Object());
        //判断两个对象是否指向不同对象
        assertNotSame(new Object(),new Object());
        //判断给定的boolean值是否为true
        assertTrue(true);
        //判断给定的boolean值是否为false
        assertFalse(false);
        //判断给定的对象引用是否为null
        assertNull(null,"给定的引用为null");
        assertNotNull("aaa");
    }

    @Test
    void test2() {
        //只有所有测试都通过，才会通过。只要有一个未通过，他就不会继续执行下去。
        //assertAll使用函数式编程
        assertAll(
                ()->assertTrue(true),
                ()->assertNotNull("aaa")
        );
    }
}
```

#### 前置条件

和断言的不同之处在于，在错误的情况下，前置条件不会报错，会直接跳过这个测试方法。

```java
@Test
void test3() {
    //判断是否满足
    assumeFalse(false,"值为true");
    //前面的对象满足，后面才会执行
    assumingThat(()->true,()-> System.out.println("aaa"));
}
```

#### 嵌套测试

使用@Nested注解在内部类上。

内部类的测试方法每一次执行，外面的@BeforeEach之类的修饰的方法也会执行。

内部Test可以驱动外部的方法，外部方法不可以驱动内部类的@BeforeEach之类的方法

```java
import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JunitTest {

    Stack<String> stack;

    @BeforeEach
    void test5() {
        System.out.println("stack被初始化");
        stack = new Stack<>();
    }

    @Nested
    @DisplayName("嵌套测试")
    class TestNested {
        String element = "env";

        @Test
        public void pushElement() {
            stack.push(element);
        }

        @Test
        public void popElement() {
            stack.pop();
        }

        @Test
        public void isEmpty() {
            assertTrue(stack.isEmpty());
        }

        @Test
        public void throwsExceptionWhenPeeked() {
            assertThrows(EmptyStackException.class,stack::peek);
        }
    }
}
```

#### 参数化测试

使用@ParameterizedTest注解

```java
@ParameterizedTest
@ValueSource(ints = {1,2,3,4,5})
void test4(int i) {
    System.out.println(i);
}
```

ValueSource注解可以传递多种类型的数组

```java
@ArgumentsSource(ValueArgumentsProvider.class)
public @interface ValueSource {
    short[] shorts() default {};

    byte[] bytes() default {};

    int[] ints() default {};

    long[] longs() default {};

    float[] floats() default {};

    double[] doubles() default {};

    char[] chars() default {};

    boolean[] booleans() default {};

    String[] strings() default {};

    Class<?>[] classes() default {};
}
```

可以使用MethodSource注解获取参数

```java
@ParameterizedTest
//@MethodSource("getParam1")
@MethodSource("getParam2")
void test4(String string) {
    System.out.println(string);
}

static String[] getParam() {
    return new String[]{"a","b","c","d","e"};
}

static Stream<String> getParam2() {
    return Stream.of("c","d");
}
```



### SpringBoot指标监控

> Spring Boot Actuator

1.x和2.x版本的不同

- 1.x
  - 支持SpringMVC
  - 基于继承方式扩展
  - 层级Metrics配置
  - 自定义Metrics收集
  - 默认较少的安全策略
- 2.x
  - 支持SpringMVC、JAX-RX以及Webflux
  - 注解驱动进行扩展
  - 层级&名称空间Metrics
  - 底层使用MicroMeter
  - 默认丰富的安全策略

如何使用：

1. 引入依赖

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

2. 访问：http://localhost:8080/actuator/**

   ```body
   {"_links":{"self":{"href":"http://localhost:8081/actuator","templated":false},"health":{"href":"http://localhost:8081/actuator/health","templated":false},"health-path":{"href":"http://localhost:8081/actuator/health/{*path}","templated":true}}}
   ```

   它告诉我们可以访问health/actuator/health下的路径。

   我们把actuator后面的那个单词叫监控端点（EndPoint）

   ```yaml
   management:
     endpoints:
       enabled-by-default: true  # 默认开启所有监控端点
       web:
         exposure:
           include: '*' # 以Web方式暴露所有端点
   ```

   测试端点：

   中文文档：https://www.docs4dev.com/docs/zh/spring-boot/2.1.1.RELEASE/reference/production-ready-endpoints.html

   | ID                 | Description                                                  | Enabled by default |
   | ------------------ | ------------------------------------------------------------ | ------------------ |
   | `auditevents`      | Exposes audit events information for the current application. | Yes                |
   | `beans`            | Displays a complete list of all the Spring beans in your application. | Yes                |
   | `caches`           | Exposes available caches.                                    | Yes                |
   | `conditions`       | Shows the conditions that were evaluated on configuration and auto-configuration classes and the reasons why they did or did not match. | Yes                |
   | `configprops`      | Displays a collated list of all `@ConfigurationProperties`.  | Yes                |
   | `env`              | Exposes properties from Spring’s `ConfigurableEnvironment`.  | Yes                |
   | `flyway`           | Shows any Flyway database migrations that have been applied. | Yes                |
   | `health`           | Shows application health information.                        | Yes                |
   | `httptrace`        | Displays HTTP trace information (by default, the last 100 HTTP request-response exchanges). | Yes                |
   | `info`             | Displays arbitrary application info.                         | Yes                |
   | `integrationgraph` | Shows the Spring Integration graph.                          | Yes                |
   | `loggers`          | Shows and modifies the configuration of loggers in the application. | Yes                |
   | `liquibase`        | Shows any Liquibase database migrations that have been applied. | Yes                |
   | `metrics`          | Shows ‘metrics’ information for the current application.     | Yes                |
   | `mappings`         | Displays a collated list of all `@RequestMapping` paths.     | Yes                |
   | `scheduledtasks`   | Displays the scheduled tasks in your application.            | Yes                |
   | `sessions`         | Allows retrieval and deletion of user sessions from a Spring Session-backed session store. Not available when using Spring Session’s support for reactive web applications. | Yes                |
   | `shutdown`         | Lets the application be gracefully shutdown.                 | No                 |
   | `threaddump`       | Performs a thread dump.                                      | Yes                |

   ```yaml
   management:
     endpoints:
       enabled-by-default: true  # 默认开启所有监控端点
       web:
         exposure:
           include: '*' # 以Web方式暴露所有端点
     endpoint: # 对某个端点的单独配置
       health:
         show-details: always
   ```

3. 常用端点

   health endpoint

   查看端点的健康状态

   metrics endpoint

   提供详细的、层级的、空间指标信息，这些信息可以被pull和push方式得到

4. 如果有些端点是隐私的，不能公开，我们可以把全部的关闭，用谁开谁

   ```yaml
   management:
     endpoints:
       enabled-by-default: false
       web:
         exposure:
           include: '*' # 以Web方式暴露所有端点
     endpoint: # 对某个端点的单独配置
       health:
         show-details: always
         enabled: true
       info:
         enabled: true
       beans:
         enabled: true
       metrics:
         enabled: true
   ```

5. 定制Health信息

   实现HealthIndicator接口或者继承AbstractHealthIndicator抽象类

   ```java
   /**
    * 组件名字默认是HealthIndicator前面的myCom。
    * 可以从http://localhost:8080/actuator/health查看myCom的状态
    */
   @Component
   public class MyComHeathIndicator extends AbstractHealthIndicator {
       /**
        * 检查健康状况
        * @param builder
        * @throws Exception
        */
       @Override
       protected void doHealthCheck(Health.Builder builder) throws Exception {
           //比如mongodb 获取连接测试
           Map<String,Object> map = new HashMap<>();
           if (true) {
               //健康
               builder.up();
           } else {
               builder.down();
               map.put("msg","连接超时");
               map.put("ms",500);
   
           }
           //返回数据给它
           builder.withDetail("code",100)
                   .withDetails(map);
       }
   }
   ```

6. 定制Info信息

   > 方式一：

   可以直接在application.yml中定义

   ```yaml
   info:
     appName: boot-admin
     appVirsion: 1.0.0
     # 可以直接从maven中动态获取
     mavenProjectName: @project.artifactId@
     mavenProjectVersion: @project.version@
   ```

   > 方式二：

   实现InfoContributor接口

   ```java
   @Component
   public class AppInfoInfoContributor implements InfoContributor {
   
       @Override
       public void contribute(Info.Builder builder) {
           builder.withDetail("msg","你好")
                   .withDetails(Collections.singletonMap("name","zhangsan"));
       }
   }
   ```

7. 定制Metrics

   ```java
   @Bean
   MeterBinder queueSize(Queue queue) {
       return (registry) -> Gauge.builder("queueSize",queue::size).registry(registry);
   }
   ```

   也可以使用其它方式...

8. 定制EndPoint

   需要被放在容器中（@Component）

   @EndPoint(id="")注解标注

   @ReadOperation：端点的读操作

   @WriteOperation：端点的写操作

   ```java
   @Component
   @Endpoint(id = "myEndPoint")
   public class DockerEndPoint {
   
       @ReadOperation
       public Map getDockInfo() {
           return Collections.singletonMap("message","hhh");
       }
   
       @WriteOperation
       public void restartDocker() {
           System.out.println("Docker restarted");
       }
   }
   ```

9. 指标监控Admin Server

   导入依赖

   ```xml
   <!--如果版本太低的话可能不兼容springboot-->
   <!--服务端导入依赖-->
   <dependency>
       <groupId>de.codecentric</groupId>
       <artifactId>spring-boot-admin-starter-server</artifactId>
       <version>2.7.0</version>
   </dependency>
   
   <!--客户端导入依赖-->
   <dependency>
       <groupId>de.codecentric</groupId>
       <artifactId>spring-boot-admin-starter-client</artifactId>
       <version>2.7.0</version>
   </dependency>
   ```

   @EnableAdminServer放在服务端启动类上，开启admin监控功能

   客户端需要配置，这个8888是服务端端口号

   ```yaml
   spring:
     boot:
       admin:
         client:
         # url是adminserver的地址
           url: http://localhost:8888
   ```

   最后登录服务端可以看到详情：

   http://localhost:8888/

   <img src="https://ibb.co/4KZjFLB">

   ![屏幕截图 2022-07-28 155358](F:/typroc/Typora/图片2/屏幕截图 2022-07-28 155358.png)

### Profile

yml配置文件可以查找的位置

- classpath根路径
- classpath根路径下的config目录
- jar包当前目录
- jar包当前目录的config目录
- /config子目录的直接子目录
- 加载顺序：上面的五个中，下面的可以覆盖上面的，例如classpath根路径下的config目录中的yml，可以覆盖classpath根路径下的yml。

环境切换、条件装配、Profile分组

### 外部化配置

- Java属性文件
- yml文件
- 环境变量
- 命令行参数

### 自定义starter

项目启动时自动加载META-INF下的spring.factories里定义的配置文件。

想让别人引入我们的场景，首先需要一个场景启动器。场景启动器只是来说明当前有什么依赖，并不会包含源代码。
场景启动器引入自动配置包autoconfigure，自动配置包引入spring-boot-starter。

> 自定义举例

1. 场景启动器：hello-spring-boot-starter
2. 自动配置包：hello-spring-boot-starter-autoconfigure

首先创建一个空项目，再在空项目中创建两个模块，一个是spring Initializr，另一个是maven模块

> 自动配置包：hello-spring-boot-starter-autoconfigure

用spring初始化模块创建

首先需要一个外部的接口，给外部调用

```java
package com.van.hello.service;

import com.van.hello.bean.HelloProperties;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 默认不要放在容器中，因为不一定要放容器，可以写一个自动配置类来确定是否放入
 */
public class HelloService {

    @Autowired
    HelloProperties helloProperties;
    
    public String sayHello(String userName) {
        return helloProperties.getPrefix() + ":" + userName + ":" + helloProperties.getSuffix();
    }
}
```

注入配置类：HelloProperties

```java
package com.van.hello.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
//自定义配置前缀
@ConfigurationProperties("vae.test")
public class HelloProperties {
    private String prefix;
    private String suffix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
```

启动项目的时候会先去resources下的META-INF下的spring.factories中查看哪些需要自动配置。所以我们还需要建一个自动配置类，并将这个自动配置类配置在spring.factories中。

自动配置类：HelloServiceAutoConfiguration

```java
package com.van.hello.auto;

import com.van.hello.bean.HelloProperties;
import com.van.hello.service.HelloService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HelloProperties.class)
public class HelloServiceAutoConfiguration {

    //当容器中没有HelloService的时候才注入
    @ConditionalOnMissingBean(HelloService.class)
    @Bean
    public HelloService helloService() {
        return new HelloService();
    }
}
```

spring.factories配置

```factories
#Auto Config
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.van.hello.auto.HelloServiceAutoConfiguration
```

pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
   <modelVersion>4.0.0</modelVersion>
   <parent>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-parent</artifactId>
      <version>2.7.2</version>
      <relativePath/> <!-- lookup parent from repository -->
   </parent>
   <groupId>com.van</groupId>
   <artifactId>hello-spring-boot-starter-autoconfigure</artifactId>
   <version>0.0.1-SNAPSHOT</version>
   <name>hello-spring-boot-starter-autoconfigure</name>
   <description>Demo project for Spring Boot</description>
   <properties>
      <java.version>11</java.version>
   </properties>
   <dependencies>
      <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter</artifactId>
      </dependency>
   </dependencies>
</project>
```

autoconfigure配置完后需要install到maven仓库（用Lifestyle的可以，用Plugin的install就不行，不知道为什么，可能是版本问题）

> 场景启动器：hello-spring-boot-starter

用maven模块创建，只需要导入上面自动配置包的依赖即可

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.van</groupId>
    <artifactId>hello-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>com.van</groupId>
            <artifactId>hello-spring-boot-starter-autoconfigure</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

同样的install到maven仓库中。



> 外部模块测试

新建一个spring Initializr项目

导入场景启动器依赖

```xml
<dependency>
    <groupId>com.van</groupId>
    <artifactId>hello-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
controller编写

```java
package com.van.testauto.controller;

import com.van.hello.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    //注入自动配置好的HelloService，如果自己在config中重新配置了HelloService可以覆盖自动配置的。因为写了@ConditionalOnMissingBean注解
    @Autowired
    HelloService helloService;

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return helloService.sayHello("vae");
    }
}
```

同时在配置文件中配置属性

```yaml
vae:
  test:
    prefix: hello
    suffix: are you ok
server:
  port: 8081
```



> 补充

dependencyManagement

dependencyManagement元素提供了一种管理依赖版本号的方式。在dependencyManagement元素中声明所依赖的jar包的版本号等信息，那么所有子项目再次引入此依赖jar包时则无需显式的列出版本号。Maven会沿着父子层级向上寻找拥有dependencyManagement 元素的项目，然后使用它指定的版本号。

使用parent方式会导致很多版本冲突，这很不好，可以使用dependencyManagement方式

父类中定义

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>1.2.3.RELEASE</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

子类则无需指定版本信息

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```



官方文档中这样写 **Using Spring Boot without the Parent POM**

The preceding sample setup does not let you override individual dependencies by using a property, as explained above. To achieve the same result, you need to add an entry in the `dependencyManagement` of your project **before** the `spring-boot-dependencies` entry. For instance, to upgrade to another Spring Data release train, you could add the following element to your `pom.xml`:

```xml
<dependencyManagement> 
	<dependencies> 
		<!-- 覆盖Spring Boot提供的Spring Data release train --> 
		<dependency> 
			<groupId> org.springframework.data </groupId> 
			<artifactId> spring-data-releasetrain </artifactId> 
			<版本> Fowler-SR2 </version> 
			<type> pom </type> 
			<scope> import </scope> 
		</dependency> 
		<dependency> 
			<groupId> org.springframework.boot </groupId> 
			<artifactId> spring-boot -dependencies </artifactId> 
			<version>2.0.4.RELEASE </version> 
			<type> pom</type> 
			<scope>导入</scope> 
		</dependency> 
	</dependencies> 
</dependencyManagement>
```

