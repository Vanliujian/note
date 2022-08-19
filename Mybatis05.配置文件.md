```xml
<!--
    environments: 配置多个连接数据库的环境
    default: 设置使用的默认环境,对应environment的id
-->
<environments default="development">
    <!--
        environment: 配置某个具体的环境属性
        id: 表示连接数据库的环境的唯一标识,不能重复
    -->
    <environment id="development">
        <!--
            transactionManager: 设置数据管理方式
                type="JDBC/MANAGED"
                jdbc:表示当前环境中执行sql时,使用的是jdbc连接
                managed:被管理,如spring
        -->
        <transactionManager type="JDBC"/>
        <!--
                dataSource: 配置数据源

            -->
        <dataSource type="POOLED">
            <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
            <property name="url" value="jdbc:mysql://localhost:3306/mybatis_study?serverTimezone=UTC"/>
            <property name="username" value="root"/>
            <property name="password" value="liujian"/>
        </dataSource>
    </environment>
</environments>
```

