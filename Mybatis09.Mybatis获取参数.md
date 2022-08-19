获取参数的两种方式

1. ${}
   - 本质是字符串拼接
2. #{}
   - 本质是占位符赋值



> 获取参数的第一种情况

mapper接口方法的参数为单个字面量类型

这个#{}里面可以写任意字符串,但是建议使用对应的名称.

```xml
<select id="selectUserById" resultType="User">
	select * from t_user where id = #{id};
</select>
```

```bash
使用${}符号的时候,如果要的是string类型的,那么就需要在外面加个单引号'${username}'
```

```xml
<select id="selectUserById" resultType="User">
	select * from t_user where id = ${id};
</select>
```



> 如果是多个参数

此时mybatis会将参数放在一个map集合中,以arg0,arg1或param1,param2为key.

获取方式

```xml
<select id="selectUserByIdAndName" resultType="User">
	select * from t_user where id = #{arg0} and username = #{arg1};
</select>
```



> 如果有多个参数还可以自己设置键值对进行访问

```java
@Test
public void testSelectByIdAndUsername() {
    SqlSession sqlSession = SqlSessionUtils.getSqlSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    HashMap<String, Object> map = new HashMap<>();
    map.put("id",3);
    map.put("username","lisi");
    User lisi = mapper.selectUserByIdAndUsername(map);
    System.out.println(lisi);
}
```

```xml
<select id="selectUserByIdAndUsername" resultType="User">
    select * from t_user where id = #{id} and username = #{username};
</select>
```



> 传递过来的是实体类型

如果传递的参数是实体类型，可以通过实体类型的属性直接调用，这里的属性指的是get/set方法后面的字段开头字母小写得来的。

```java
void insertByUser(User user);

<select id="insertByUser">
    insert into t_user values(null,#{username},#{password},#{age},#{sex},#{email});
</select>

@Test
public void testInsertByUser() {
    SqlSession sqlSession = SqlSessionUtils.getSqlSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    mapper.insertByUser(new User("wangwu", "12345", 15, "m", "12345@55.com"));
}
```



> 使用@Param注解

注解写在接口类中

~~~java
User selectByIdAndNameUseParam(@Param("id") int id,@Param("username") String username);
~~~

~~~java
//获取的时候通过注解的值来获取
<select id="selectByIdAndNameUseParam" resultType="User">
    select * from t_user where id = #{id} and username = #{username};
</select>
~~~

~~~java
@Test
public void selectByIdAndUsernameUseParam() {
    SqlSession sqlSession = SqlSessionUtils.getSqlSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    User lisi = mapper.selectByIdAndNameUseParam(3, "lisi");
    System.out.println(lisi);
}
~~~

- @Param注解还可以通过param1，param2来获取参数

> 总结

总的来说可以只分为两种方式

1. 通过Param注解（一个或者多个参数都可以用这个）
2. 传入的是实体类对象

