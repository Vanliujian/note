# 多对一映射

> 多对一映射

association标签：

- 应用场景：当要映射过来的实体类中包含另外一个实体类对象

- 学生实体类中放学生成绩的对象

```java
<resultMap id="result2" type="Student">
    <id property="id" column="id"/>
    <result property="name" column="username"/>
    <association property="studentScore" javaType="com.van.pojo.StudentScore">
        <id property="sid" column="sid"/>
        <result property="chinese" column="chinese"/>
		<result property="math" column="math"/>
    </association>
</resultMap>
```



分步查询

- 同样使用使用association标签

  - 先创建一个查询成绩的map

    - type:返回值类型是StudentScore

    ```java
    <resultMap id="result" type="StudentScore">
        <id property="sId" column="s_Id"/>
        <result property="chinese" column="chinese"/>
        <result property="math" column="math"/>
    </resultMap>
    ```

  - 再在另外一个xml中创建查询学生的map

    - 返回值类型是Student
    - association
      - property：Student中成绩属性对应的名称
      - select：对应的查询方法，一个全限定方法名
      - column:这个column和其它的不一样，这个column指的是两者共有的属性，可以通过它来找另外一个对应的数据（可以理解为外键）

    ```java
    <resultMap id="result2" type="Student">
        <id property="id" column="id"/>
        <result property="name" column="username"/>
        <association property="studentScore"
        	select="com.van.mapper.StudentScoreMapper.selectScore"
        	column="sId"/>
    </resultMap>
    ```


# 一对多映射

比如学生会表中包含了一些学生，这时候可以把学生信息放在学生会中的一个集合。

- 使用connection：ofType属性，集合中的数据类型

  left join查询

- 分布查询

  和多对一差不多，只是association变成了connection