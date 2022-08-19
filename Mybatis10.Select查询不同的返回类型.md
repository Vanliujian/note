1. 查询返回结果是一个实体类对象

   可以通过实体类对象接收

   可以通过List集合接收

   可以通过Map接收

   ```java
   Map<String,Object> selectByIdToMap(@Param("id") int id);
   
   <select id="selectByIdToMap" resultType="map">
       select * from t_user where id = #{id};
   </select>
   
   @Test
   public void selectByIdToMap() {
       SqlSession sqlSession = SqlSessionUtils.getSqlSession();
       UserMapper mapper = sqlSession.getMapper(UserMapper.class);
       Map<String, Object> map = mapper.selectByIdToMap(3);
       System.out.println(map);
   }
   ```

   

2. 查询返回结果是多个实体类对象

   这时候就需要用集合来接收

   1. 使用List集合接收

      ```java
      List<User> selectAllUser();
      ```

      ```java
      <select id="selectAllUser" resultType="User">
      	select * from t_user;
      </select>
      ```

      ```java
      @Test
      public void testSelectAllUser() {
          SqlSession sqlSession = SqlSessionUtils.getSqlSession();
          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          List<User> users = mapper.selectAllUser();
          users.forEach(user -> System.out.println(user));
      }
      ```

   2. 使用Map集合接收

      通过@MapKey("")注解,里面传入要作为key的属性值,且要唯一

      ```java
      @MapKey("id")
      Map<String,Object> selectAllToMap();
      ```

      ```java
      <select id="selectAllToMap" resultType="map">
          select * from t_user;
      </select>
      ```

      ```java
      @Test
      public void selectAllToMap() {
          SqlSession sqlSession = SqlSessionUtils.getSqlSession();
          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          Map<String, Object> map = mapper.selectAllToMap();
          System.out.println(map);
      }
      ```

      

mybatis中设置了一些默认的类型别名

- Integer
- String
- byte
- map等