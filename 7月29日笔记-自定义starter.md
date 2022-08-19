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

