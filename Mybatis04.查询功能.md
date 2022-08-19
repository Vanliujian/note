# 查询

Mybatis查询时，配置文件中必须有返回值类型。resultType或者resultMap

```xml
<select id="selectUserById" resultType="com.van.pojo.User">
    select * from t_user where id = 3;
</select>

<select id="selectAllUser" resultType="com.van.pojo.User">
    select * from t_user;
</select>
```

- resultType和resultMap的区别

  resultMap是在数据库属性名和实体类变量名不一样的时候进行一一映射，如果一样的只需要设置resultType进行默认映射就可以了。

- select方法必须有返回值

```java
@Test
public void testSelectAll() throws IOException {
    InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
    SqlSessionFactory build = new SqlSessionFactoryBuilder().build(is);
    SqlSession sqlSession = build.openSession(true);
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    List<User> users = mapper.selectAllUser();
    users.forEach(user -> System.out.println(user));
}
```