# Spring集成Web环境

## ApplicationContext

ApplicationContext应用上下文，是通过new ClassPathXmlApplicationContext(配置文件)获取的，但是每次都这样找会很麻烦，应用上下文对象被创建多次。

在Web项目中可以通过ServletContextLinstener监听web启动。可以在web应用启动时就加载Spring配置文件。创建应用上下文对象，并将其存储到最大的ServletContext域中。

Spring帮我们封装了这样的方法，我们只需要做两件事

1. 在web.xml中配置ContextLoaderListener监听器(导入spring-web坐标)
2. 使用WebApplicationContextUtils获得应用上下文对象ApplicationContext

```xml
<!--全局初始化参数-->
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
</context-param>

<!--配置监听器-->
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```



# SpringMvc

## 快速步骤

1. 导入SpringMvc坐标

   spring-webmvc

2. 配置核心控制器DispatcherServlet

   web.xml中

   ```xml
   <!--配置SpringMvc前端控制器-->
   <servlet>
       <servlet-name>DispatcherServlet</servlet-name>
       <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
       <load-on-startup>1</load-on-startup>
   </servlet>
   <!--配置映射地址-->
   <servlet-mapping>
       <servlet-name>DispatcherServlet</servlet-name>
       <url-pattern>/</url-pattern>
   </servlet-mapping>
   ```

   SpringMVC扫描controller包

3. 创建Controller类和视图页面

4. 使用注解配置Controller类中业务方法的映射地址

5. 配置spring-mvc.xml核心配置文件

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns:context="http://www.springframework.org/schema/context"
          xsi:schemaLocation=
                  "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                   http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
       <context:component-scan base-package="com.van.controller"/>
   </beans>
   ```

   spring-mvc.xml配置完后，需要加载。是前端控制器需要使用spring-mvc.xml，所以要把它配置在前端控制器中，在web.xml中

   ```xml
   <servlet>
       <servlet-name>DispatcherServlet</servlet-name>
       <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
       <init-param>
           <param-name>contextConfigLocation</param-name>
           <param-value>classpath:spring-mvc.xml</param-value>
       </init-param>
       <load-on-startup>1</load-on-startup>
   </servlet>
   <servlet-mapping>
       <servlet-name>DispatcherServlet</servlet-name>
       <url-pattern>/</url-pattern>
   </servlet-mapping>
   ```

6. 客户端发起请求测试

## 视图解析器配置

> spring-mvc.xml还可以配置视图解析器，配置完成后在servlet中访问的时候可以直接return文件名

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation=
               "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
    <context:component-scan base-package="com.van.controller"/>
    <!--配置内部资源视图解析器-->
    <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/jsp/"></property>
        <property name="suffix" value=".jsp"></property>
    </bean>
</beans>
```

## SpringMVC数据响应方式

1. 页面跳转

   1. 直接返回字符串

      - 这种方式会将返回的字符串与视图解析器的前后缀拼接后跳转

   2. 通过ModelAndView对象返回

      - ModelAndView可以设置返回的数据和返回的页面是什么

        ```java
        //这里在视图解析器中prex配置了/，suffix配置了.jsp
        @RequestMapping("save2")
        public ModelAndView save2() {
            ArrayList<Object> list = new ArrayList<>();
            list.add("hello");
            list.add("how are you");
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("username",list);
            //返回的视图页面是index页面
            modelAndView.setViewName("index");
            return modelAndView;
        }
        ```

      - ModelAndView也可以放在save方法的参数中

      - 可以方法参数中放Model，返回的是字符串，Model中的数据照样能获取到

      - 也可以在方法参数中传入HttpServletRequest

2. 回写数据

   1. 直接返回字符串

      - 方式一

        通过传入HttpServletResponse，调用getWriter().print("");返回值设为void

      - 方式二

        使用@ResponseBody注解，直接返回

        @ResponseBody：告知SpringMVC框架不进行视图跳转，直接进行数据响应

      - 返回json格式，使用Jackson工具

        1. 先导入依赖（3个）

           ```xml
           <dependency>
               <groupId>com.fasterxml.jackson.core</groupId>
               <artifactId>jackson-core</artifactId>
               <version>2.13.3</version>
           </dependency>
           <dependency>
               <groupId>com.fasterxml.jackson.core</groupId>
               <artifactId>jackson-databind</artifactId>
               <version>2.13.3</version>
           </dependency>
           <dependency>
               <groupId>com.fasterxml.jackson.core</groupId>
               <artifactId>jackson-annotations</artifactId>
               <version>2.13.3</version>
           </dependency>
           ```

        2. Controller类中

           ```java
           @RequestMapping("save3")
           @ResponseBody
           public String save3() throws JsonProcessingException {
               User lisi = new User("lisi", 24);
               ObjectMapper mapper = new ObjectMapper();
               String string = mapper.writeValueAsString(lisi);
               return string;
           }
           ```

   2. 返回对象或集合

      - 可以配置自动进行json转换（处理器映射器），不用再像上面一样导入依赖，使用ObjectMapper

        ```xml
        <!--配置处理器映射器-->
        <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
            <property name="messageConverters">
                <list>
                    <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
                </list>
            </property>
        </bean>
        ```

        直接返回User即可，配置了json的处理器映射器后，SpringMVC会自动帮我们进行json转换

        ```java
        @RequestMapping("save4")
        @ResponseBody
        public User save4() throws JsonProcessingException {
            User lisi = new User("lisi", 24);
            return lisi;
        }
        ```

      - mvc:annotation-driven注解驱动

        上面的配置还是很繁琐，使用mvc:annotation可以省略很多步骤

        mvc:annotation-driven:

        - 在spring-mvc.xml中配置

        - 默认底层会集成jackson进行对象或集合的json格式字符串的转换，且使用它时候，自动加载ReuestMappingHandlerMapping(处理映射器)和RequestMappingHandlerAdapter(处理适配器)

          ```xml
          <!--有了它后，下面的处理器映射器就不需要了，它会自动配置jason转换-->
          <mvc:annotation-driven/>
          
          <!--配置处理器映射器-->
          <!--    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">-->
          <!--        <property name="messageConverters">-->
          <!--            <list>-->
          <!--                <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>-->
          <!--            </list>-->
          <!--        </property>-->
          <!--    </bean>-->
          ```


### 获取请求数据

#### 获取请求参数

1. 基本类型参数，如果请求参数名和接收的变量名一致，会自动映射

   ```java
   @RequestMapping("/save5")
   @ResponseBody
   public void save5(String username,int age) {
       System.out.println(username+":"+age);
   }
   ```

2. POJO类型参数

   同样如果请求参数和实体类属性名的(set方法名除去set后面第一个字母大写变小写)相同会自动映射，如果同一个参数有多个值会用逗号拼接。

   ```java
   @RequestMapping("/save6")
   @ResponseBody
   public void save6(User user) {
       System.out.println(user);
   }
   ```

3. 数组类型参数

   ```java
   @RequestMapping("/save7")
   @ResponseBody
   public void save7(String[] array) {
       System.out.println(Arrays.asList(array));
   }
   ```

4. 集合类型参数

   1. 一般用一个新的对象把集合当作属性存储

      ```java
      public class VO {
          private List<User> userList;
      
          public VO() {
          }
      
          public VO(List<User> userList) {
              this.userList = userList;
          }
          //get And set toString
      }
      ```

      ```java
      @RequestMapping("/save8")
      @ResponseBody
      public void save8(VO vo) {
          System.out.println(vo);
      }
      ```

      前端传递过来

      ```html
      <html>
      <body>
      <form action="/spring_mvc_project/save8" method="post">
          <input type="text" name="userList[0].username">
          <input type="text" name="userList[0].age">
          <input type="text" name="userList[1].username">
          <input type="text" name="userList[1].age">
          <input type="submit" value="提交">
      </form>
      </body>
      </html>
      ```

      输出：VO{userList=[User{username='zhangsan', age=23}, User{username='lisi', age=24}]}

   2. 直接将List作为参数

      这种方式需要前端使用ajax请求发送json格式数据，并使用@ResquestBody注解

      ```java
      @RequestMapping("/save9")
      @ResponseBody
      public void save9(@RequestBody List<User> users) {
          System.out.println(users);
      }
      ```

      小知识：

      开放资源的访问

      ```xml
      <!--找这些资源的时候就不需要去DispatcherServlet映射处理了-->
      <mvc:resources mapping="/js/**" location="/js/"/>
      
      <!--也可以使用这个方法-->
      <mvc:default-servlet-handler/>
      ```

5. @RequestParam注解

   - value：设置请求参数名，解决请求参数名和接收属性名不一致问题
   - required：请求参数value的值是否必须包括，默认是true
   - defaultValue：当没有指定请求参数时，默认用这个值赋值

   ```java
   @RequestMapping(".save10")
   @ResponseBody
   public void save10(@RequestParam(value = "name",required = true,defaultValue = "lisi") String username) {
       System.out.println(username);
   }
   ```

6. @RequestHeader

   获取请求头数据，根据具体的Key获取，可以用F12抓包查看

   ```java
   @RequestMapping("/save13")
   @ResponseBody
   public void save13(@RequestHeader("User-Agent") String header) {
       System.out.println(header);
   }
   ```

7. @CookieValue

   获取cookie，根据前端的cookieKey获取

   ```java
   @RequestMapping("/save14")
   @ResponseBody
   public void save14(@CookieValue("SERVERID") String serverId) {
       System.out.println(serverId);
   }
   ```

8. 文件上传

   三要素：

   - 表单项type=”file“
   - 表单提交方式：post
   - 表单的enctype属性是多部分表单形式，即enctype=”multipart/form-data“

   ```jsp
   <form action="/spring_mvc_project/save15" method="post" enctype="multipart/form-data">
       <input type="text" name="name"/> <br/>
       <input type="file" name="file"/><br/>
       <input type="submit" value="提交">
   </form>
   ```

   - 导入依赖

     ```xml
     <dependency>
         <groupId>commons-fileupload</groupId>
         <artifactId>commons-fileupload</artifactId>
         <version>1.2.2</version>
     </dependency>
     <dependency>
         <groupId>commons-io</groupId>
         <artifactId>commons-io</artifactId>
         <version>2.4</version>
     </dependency>
     ```

   - spring-mvc.xml中配置文件上传解析器

     ```xml
     <!--配置文件上传解析器-->
     <!--这里必须配置一个id为multipartResolver不能是其它的，让springmvc来管理-->
     <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
         <!--默认字符集编码-->
         <property name="defaultEncoding" value="UTF-8"/>
         <!--文件最大上传大小-->
         <property name="maxUploadSize" value="5000000"/>
         <!--单个文件最大上传大小-->
         <property name="maxUploadSizePerFile" value="10000"/>
     </bean>
     ```

   - controller接收

     ```java
     @RequestMapping(value = "/save15")
     @ResponseBody
     public void save15(MultipartFile file) throws IOException {
         //getName获取的是前端对应的name
         System.out.println(file.getName());
         //getOriginalFilename获取的是文件的名称
         String originalFilename = file.getOriginalFilename();
         System.out.println(originalFilename);
     	//把文件保存在E/upload文件夹下，upload必须已经存在    
         file.transferTo(new File("E:\\upload\\"+originalFilename));
     }
     ```

     output:

     ​	file
     ​	新建文本文档.txt

     

### 乱码问题

- 可以使用一个过滤器解决

  在web.xml中配置

  ```xml
  <filter>
      <filter-name>CharacterEncodingFilter</filter-name>
      <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
      <init-param>
          <param-name>encoding</param-name>
          <param-value>UTF-8</param-value>
      </init-param>
  </filter>
  <filter-mapping>
      <filter-name>CharacterEncodingFilter</filter-name>
      <url-pattern>/*</url-pattern>
  </filter-mapping>
  ```

### 类型转换器

1. 什么是类型转换器

   像springmvc内部封装了类型转换器，Controller中传入的是String，我们用int接收，它会自动转换为int。传入的是日期类型2022/7/20，用Date接收，会自动转换为日期类型。

2. 如果springmvc满足不了我们的转换该怎么办？比如传入的日期类型是2022-7-20，这时候就需要我们自己定义类型转换器了。

3. 自定义类型转换器

   - 定义转换器类实现Conventer接口

     两个泛型参数：

     第一个表示传入的数据类型

     第二个表示需要转换成的类型

     ```java
     public class DateConverter implements Converter<String, Date> {
         @Override
         public Date convert(String stringDate) {
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
             Date date = null;
             try {
                 date = simpleDateFormat.parse(stringDate);
             } catch (ParseException e) {
                 e.printStackTrace();
             }
             return date;
         }
     }
     ```

   - 在配置文件中申明转换器

     ```xml
     <bean id="conversionService" class="org.springframework.context.support.ConversionServiceFactoryBean">
         <property name="converters">
             <list>
                 <bean class="com.van.converter.DateConverter"></bean>
             </list>
         </property>
     </bean>
     ```

   - 在<annotation-driven>中引用转换器

     ```xml
     <!--mvc注解驱动-->
     <mvc:annotation-driven conversion-service="conversionService"/>
     ```

   - 此时比如用get方法传入参数2022-7-20就可以接收到了

     localhost:8080/spring_mvc_project/save12?date=2022-7-20

     ```java
     @RequestMapping("/save12")
     @ResponseBody
     public void save12(@RequestParam("date") Date date) {
         System.out.println(date);
     }
     ```



# Spring JdbcTemplate

- Spring对jdbc的封装，更方便对数据库操作

1. 使用步骤

   - 导入依赖

     ```xml
     <!--spring操作数据库依赖-->
     <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-jdbc</artifactId>
         <version>5.3.1</version>
     </dependency>
     <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-tx</artifactId>
         <version>5.3.1</version>
     </dependency>
     ```

   - 执行sql

     ```java
     @Test
     public void test1() {
         DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
         driverManagerDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
         driverManagerDataSource.setUrl("jdbc:mysql://localhost:3306/test");
         driverManagerDataSource.setUsername("root");
         driverManagerDataSource.setPassword("liujian");
         JdbcTemplate template = new JdbcTemplate();
         template.setDataSource(driverManagerDataSource);
         //TODO template.query(...);
         //TODO template.update(...);
     }
     ```

   web.xml方式

   - 在applicationContext中把它封装为一个Bean对象

   ```xml
   <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource">
       <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"></property>
       <property name="url" value="jdbc:mysql://localhost:3306/mybatis_study?serverTimezone=UTC"></property>
       <property name="username" value="root"></property>
       <property name="password" value="liujian"></property>
   </bean>
   <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
       <property name="dataSource" ref="dataSource"></property>
   </bean>
   ```

   其中的value值一般需要抽取到jdbc.properties中，这里没抽取

   ​	xml中加载properties文件，需要使用context命名空间

   ​	<context:property-placeholder location="classpath:jdbc.properties">

   - 调用

   ```java
   @Test
   public void test2() {
       ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
       JdbcTemplate template = context.getBean(JdbcTemplate.class);
       template.update("insert into t_user2 values (?,?,?)",3,"wangwu","123");
   }
   ```

2. Test测试类

   @RunWith

   - 运行器

   @Contextconfiguration

   ```java
   package com.van.test;
   
   import com.van.domain.User2;
   import org.junit.Test;
   import org.junit.runner.RunWith;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.jdbc.core.BeanPropertyRowMapper;
   import org.springframework.jdbc.core.JdbcTemplate;
   import org.springframework.test.context.ContextConfiguration;
   import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
   
   import java.util.List;
   
   @RunWith(SpringJUnit4ClassRunner.class)
   @ContextConfiguration("classpath:applicationContext.xml")
   public class JdbcCRUDTest {
       @Autowired
       private JdbcTemplate jdbcTemplate;
   
       @Test
       public void testInsert() {
           jdbcTemplate.update("insert into t_user2 values(?,?,?)",4,"lisi&a",24);
       }
       @Test
       public void testUpdate() {
           jdbcTemplate.update("update t_user2 set username = ? where id = ?","temp",222);
       }
       @Test
       public void testDelete() {
           jdbcTemplate.update("delete from t_user2 where id = ?",222);
       }
   
       @Test
       public void testQueryAll() {
           List<User2> userList = jdbcTemplate.query("select * from t_user2", new BeanPropertyRowMapper<>(User2.class));
           System.out.println(userList);
       }
   
       @Test
       public void testQueryOne() {
           User2 user = jdbcTemplate.queryForObject("select * from t_user2 where id = ?", new BeanPropertyRowMapper<>(User2.class), 2);
           System.out.println(user);
       }
   }
   ```

# Restful

1. Restful是什么

   一种软件架构风格，使用url+请求方式表示一次请求。

2. HTTP协议中四个操作方式

   GET：获取资源

   POST：新建资源

   PUT：更新资源

   DELETE：删除资源

3. 举例

   /user/1 GET：得到id为1的user

   /user/1 DELETE：删除id为1的user

   /user/1 PUT：更新id为1的user

   /user/ POST：新增user

4. 使用

   在请求地址中携带参数，并在RequestMapping中用{}接收，使用@PathVariable映射

   ```java
   @RequestMapping("/save11/{name}")
   @ResponseBody
   public void save11(@PathVariable("name") String username) {
       System.out.println(username);
   }
   ```

# Spring环境搭建

1. 创建工程

2. 导入静态页面

3. 导入坐标

4. 创建包结构（controller、service、dao、domain/pojo、utils）

5. 创建pojo实体类

6. 创建配置文件（applicationContext.xml、spring-mvc.xml、jdbc.properties、log4j.properties）

   applicationContext.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns:context="http://www.springframework.org/schema/context"
          xsi:schemaLocation="
          http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
          http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
       <!--导入外部文件-->
       <context:property-placeholder location="jdbc.properties"/>
       <!--配置数据源-->
       <bean id="datasource" class="com.alibaba.druid.pool.DruidDataSource">
           <property name="driverClassName" value="${jdbc.driver}"/>
           <property name="url" value="${jdbc.url}"/>
           <property name="username" value="${jdbc.username}"/>
           <property name="password" value="${jdbc.password}"/>
       </bean>
       <!--配置jdbcTemplate模板-->
       <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
           <property name="dataSource" ref="datasource"/>
       </bean>
   </beans>
   ```

   spring-mvc.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns:context="http://www.springframework.org/schema/context"
          xsi:schemaLocation="
          http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
          http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
       <context:component-scan base-package="com.van.controller"/>
       <!--静态资源开放-->
       <mvc:annotation-driven/>
   <!--    视图解析器，没有配置-->
   </beans>
   ```

   web.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
            version="4.0">
       <context-param>
           <param-name>contextConfigLocation</param-name>
           <param-value>classpath:applicationContext.xml</param-value>
       </context-param>
       <listener>
           <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
       </listener>
   
       <!--配置前端控制器-->
       <servlet>
           <servlet-name>DispatcherServlet</servlet-name>
           <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
           <init-param>
               <param-name>contextConfigLocation</param-name>
               <param-value>classpath:spring-mvc.xml</param-value>
           </init-param>
       </servlet>
       <servlet-mapping>
           <servlet-name>DispatcherServlet</servlet-name>
           <url-pattern>/</url-pattern>
       </servlet-mapping>
   </web-app>
   ```







# SpringMVC异常处理



# SSM整合

## 导入依赖

```xml
spring-webmvc
spring-jdbc
spring-test
mybatis
mybatis-spring
mysql-connector-java
druid
junit
servlet-api
jackson-databind

<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>spring_mvc_project</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <name>spring_mvc_project Maven Webapp</name>
    <!-- FIXME change it to the project's website -->
    <url>http://www.example.com</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>5.3.21</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.3.21</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>5.3.21</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.6</version>
        </dependency>
		<!--mybatis整合spring的依赖，SqlSessionFactoryBean就在这个依赖中-->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>1.3.3</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.11</version>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.2.11</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.13.3</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>


        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.11</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.tomcat.maven</groupId>
                <artifactId>tomcat7-maven-plugin</artifactId>
                <version>2.2</version>
                <configuration>
                    <port>80</port>
                    <path>/</path>
                    <uriEncoding>UTF-8</uriEncoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

> xml方式

applicationContext.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="com.van.service"/>

</beans>
```

mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <properties resource="jdbc.properties"/>
    <typeAliases>
        <!--起别名的包-->
        <package name="com.van.domain"/>
    </typeAliases>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${jdbc.driver}"/>
                <property name="url" value="${jdbc.url}"/>
                <property name="username" value="${jdbc.username}"/>
                <property name="password" value="${jdbc.password}"/>
            </dataSource>
        </environment>
    </environments>
    <!--引入映射文件-->
    <mappers>
        <package name="com.van.mapper"/>
    </mappers>
</configuration>
```

spring-mvc.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd">
    <context:component-scan base-package="com.van.controller"/>
    <!--静态资源开放-->
    <mvc:annotation-driven/>
    <!--视图解析器，没有配置-->

</beans>
```

## 配置文件

- Spring
  - SpringConfig
- MyBatis
  - MybatisConfig
  - JdbcConfig
  - jdbc.properties
- SpringMvc
  - SpringMvcConfig
  - ServletConfig



@RequestMapping

1. 基本信息

   - SpringMVC接收到请求，就会来找到映射关系中对应的控制器方法来处理这个请求。

   - 各个方法的RequestMapping注解的value不能重复，否则会报错

   - value属性可以是一个字符串数组，多个地址可以处理这一个请求

   - method属性：匹配get/post/其它请求方式，method属性同样是一个数组，存放枚举类型

     也可以使用如下注解，这样就不需要设置method属性

     - @GetMapping("/registry")
     - @PostMapping
     - @PutMapping
     - @DeleteMapping

     当使用上面四个这样的注解后，地址可以一样，因为请求方式不同

   - params属性，可以传递数组

     ```java
     @RequestMapping(
     	value = "/test",
         //当前请求地址必须包含username
         //params = {"username"}
         
         //当前请求地址必须不包含username
         //params = {"!username"}
         
         //当前地址必须包含username，且值为admin
         //params = {"username = admin"}
         
         //当前地址必须包含username，且值不为admin
         params = {"username != admin"}
     )
     public String success() {
         return "success";
     }
     ```

     

2. 使用位置

   - 用在方法上

     方法上使用的时候，需要在请求路径中有这个注解的value才可以处理这个请求

   - 用在类上

     当用在类上的时候，可以认为是一个分类，在请求的时候必须要在前面加上类注解的路径，再添加方法上注解路径。

     好处是多个类中不同的方法可以使用同一个映射地址，比如一个是用户信息的list，一个是订单的list。


