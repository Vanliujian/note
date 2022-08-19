# 使用xml配置

>总配置类：mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
<!--    配置连接数据库的环境-->
    <environments default="development">
        <environment id="development">
<!--            数据管理方式-->
            <transactionManager type="JDBC"/>
<!--            使用数据库连接池-->
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/mybatis_study?serverTimezone=UTC"/>
                <property name="username" value="root"/>
                <property name="password" value="liujian"/>
            </dataSource>
        </environment>
    </environments>
<!--    引入映射文件-->
    <mappers>
        <mapper resource="mappers/UserMapper.xml"/>
    </mappers>
</configuration>
```



> Mapper配置类

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.van.mapper.UserMapper">
    <insert id="insertUser">
        insert into t_user values(null,"zhangsan","123456",23,"m","12345@qq.com");
    </insert>
</mapper>
```

# 测试运行

> 从XML中构建SqlSessionFactory

1. 每个基于Mybatis的应用都是以一个SqlSessionFactory的实例为核心的。这个实例可以通过SqlSessionFactoryBuilder获得。

   ```java
   InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
   SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
   SqlSessionFactory = sqlSessionFactoryBuilder.build(is);
   ```

> 从SqlSessionFactory中获取SqlSession并执行sql语句

```java
//如果要设置自动提交，在创建sqlSession的时候传入true
SqlSession sqlSession = sqlSessionFactory.openSession(true);
//获取UserMapper的一个实例化对象
UserMapper mapper = sqlSession.getMapper(UserMapper.class);
//通过这个实例化对象调用方法执行
int result = mapper.insertUser();
//提交事物（如果没有设置自动提交）
sqlSession.commit();
```

