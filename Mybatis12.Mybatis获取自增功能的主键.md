~~~java
void insertUser(User user);
~~~

```xml
<!--    useGeneratedKeys:表示设置当前标签中的sql使用了自增的主键
        keyProperty:将自增主键的值赋值给传输到映射文件中参数的某个属性
-->
<insert id="insertUser" keyProperty="id" useGeneratedKeys="true">
    insert into t_user values(null,#{username},#{password},#{age},#{sex},#{email});
</insert>
```

```java
@Test
public void testInsertUser() {
    SqlSession sqlSession = SqlSessionUtils.getSqlSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    User user = new User("zhaosi", "1234", 24, "m", "zhaosi@163.com");
    mapper.insertUser(user);
    System.out.println(user);
    //这里输出的时候会把id也输出出来
}
```