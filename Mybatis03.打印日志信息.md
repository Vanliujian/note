1. 导入依赖

   ```xml
   <dependency>
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
        <version>1.2.17</version>
   </dependency>
   ```

2. 配置log4j.xml配置文件，放在resources文件夹下

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
   <log4j:configuration xmlns:log4j='http://jakarta.apache.org/log4j/'>
   
       <appender name="STDOUT" class="org.apache.log4j.ConsoleAppender">
           <param name="encoding" value="UTF-8" />
           <layout class="org.apache.log4j.PatternLayout">
               <param name="ConversionPattern" value="%-5p %d{MM-dd HH:mm:ss,SSS} %m (%F:%L) \n" />
           </layout>
       </appender>
   
       <logger name="java.sql">
           <level value="debug" />
       </logger>
   
       <logger name="org.apache.ibatis">
           <level value="info" />
       </logger>
   
       <root>
           <level value="DEBUG" />
           <appender-ref ref="STDOUT"/>
       </root>
   </log4j:configuration>
   ```

3. 运行对应的方法会有日志信息

   ```bash
   DEBUG 07-05 14:12:09,480 ==>  Preparing: insert into t_user values(null,"zhangsan","123456",23,"m","12345@qq.com"); (BaseJdbcLogger.java:137) 
   DEBUG 07-05 14:12:09,541 ==> Parameters:  (BaseJdbcLogger.java:137) 
   DEBUG 07-05 14:12:09,555 <==    Updates: 1 (BaseJdbcLogger.java:137) 
   ```

   

