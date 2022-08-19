> 数据库字段名和属性名不一致的时候

以前一致的时候可以默认，如果不一致就需要改进

解决方案

1. 给字段起别名(起的别名要和pojo属性值一致)

   ```xml
   <select id="findAllUser" resultType="User">
       select id,username as name,password,age,sex,email from t_user;
   </select>
   ```

2. 使用全局配置settings

   这个settings设置在mybatis-config.xml中

   ```java
   <!--全局配置-->
   <settings>
   	<!--将下划线自动映射为驼峰，emp_name:empName-->
   	<setting name="mapUnderscoreToCamelCase" value="true"/>
   </settings>
   ```

3. 通过ResultMap设置自定义的映射关系

   ```xml
   //type:实体类
   //id:主键字段
   //result:普通字段
   //property:实体类中属性名
   //column:数据库中字段名
   <select id="findAllUser" resultMap="result">
       select * from t_user;
   </select>
   <resultMap id="result" type="com.van.pojo.User">
       <id property="id" column="id"></id>
       <result property="name" column="username"/>
       <result property="password" column="password"/>
       <result property="age" column="age"/>
       <result property="sex" column="sex"/>
       <result property="email" column="email"/>
   </resultMap>
   ```

   