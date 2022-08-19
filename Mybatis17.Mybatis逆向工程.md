# Mybatis逆向工程

1. 什么是逆向工程

   先创建数据库表，由框架负责根据数据库表，反向生成如下资源

   - Java实体类
   - Mapper接口
   - Mapper映射文件

2. 创建逆向工程步骤

   - 导入依赖
   - 创建逆向工程配置文件
   - 运行插件

3. generatorConfig配置文件

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <!DOCTYPE generatorConfiguration
           PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
           "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
   <generatorConfiguration>
       <!--targetRuntime:Mybatis3生成带条件的CRUD，Mybatis3Simple生成基本的CRUD-->
       <!-- 配置 table 表信息内容体，targetRuntime 指定采用 MyBatis3 的版本 -->
       <context id="tables" targetRuntime="MyBatis3Simple">
           <!-- 抑制生成注释，注释都是英文的，可以不让它生成 -->
           <commentGenerator>
               <property name="suppressAllComments" value="true"/>
           </commentGenerator>
           <!-- 配置数据库连接信息 -->
           <jdbcConnection driverClass="com.mysql.cj.jdbc.Driver"
                           connectionURL="jdbc:mysql://localhost:3306/mybatis_study?serverTimezone=UTC"
                           userId="root"
                           password="liujian">
           </jdbcConnection>
           <!--JavaBean的生成策略-->
           <!-- 生成 model 类，targetPackage 指定model类的包名，targetProject指定生成的model放在哪个工程下面-->
           <javaModelGenerator targetPackage="com.van.pojo"
                               targetProject="src/main/java">
               <!--是否使用子包，如果是false，那么上面的com.van.pojo就是一个包的名字，否则就是一层包-->
               <property name="enableSubPackages" value="true"/>
               <!--从数据库中拿出来的时候是否去除前后的空格-->
               <property name="trimStrings" value="true"/>
           </javaModelGenerator>
           <!-- 生成 MyBatis 的 Mapper.xml 文件，targetPackage 指定 mapper.xml 文件的
          包名， targetProject 指定生成的 mapper.xml 放在 eclipse 的哪个工程下面 -->
           <sqlMapGenerator targetPackage="com.van.mapper"
                            targetProject="src/main/resources">
               <property name="enableSubPackages" value="true"/>
           </sqlMapGenerator>
           <!-- 生成 MyBatis 的 Mapper 接口类文件,targetPackage 指定 Mapper 接口类的包
          名， targetProject 指定生成的 Mapper 接口放在 eclipse 的哪个工程下面 -->
           <javaClientGenerator type="XMLMAPPER"
                                targetPackage="com.van.mapper" targetProject="src/main/java">
               <property name="enableSubPackages" value="true"/>
           </javaClientGenerator>
           <!--需要逆向分析的表，表名对应实体类类名-->
           <table tableName="t_user" domainObjectName="User"/>
       </context>
   </generatorConfiguration>
   ```

   生成的接口是这样：有基本的五个方法

   ```java
   public interface UserMapper {
       int deleteByPrimaryKey(Integer id);
   
       int insert(User record);
   
       User selectByPrimaryKey(Integer id);
   
       List<User> selectAll();
   
       int updateByPrimaryKey(User record);
   }
   ```

4. targetRuntime为Mybatis3

   这种情况下，多了一些条件查询

   实体类就会多出一个Example结尾的类，可以用这些方法来实现条件查询等。

   有些方法后面带有Selective，带有Selective的，如果是插入语句，如果插入语句中有null，那么数据库中对应的字段就不会被插入。不带有Selective的方法，数据库中的数据就是null。

5. 使用技巧

   - 如果generator生成的sql不够用怎么办？

     - 通常是手动添加

     但是如果后来这张表又有变化了，我们要重新generator吗？那就会把之前的覆盖掉

   - 所以产生的一个问题是，表结构发生变化，通过generator重新生成代码的时候，怎样保证不会冲掉之前手动添加的代码。

     `Mybatis读取配置文件的时候是根据命名空间+select/update的id来确定唯一的sql`

     那么我们可以写一个UserManualMapper.xml来写自己手动添加的代码。原来UserMapper.xml中的不去修改它。`他们俩用同一个命名空间。`

     这样以后覆盖的都是自动生成的部分。

> 踩的一些坑

- pom.xml中最好把jdk版本，字符编码加上，否则有的时候test的时候会报错

  ```xml
  <properties>
      <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      <maven.compiler.source>11</maven.compiler.source>
      <maven.compiler.target>11</maven.compiler.target>
  </properties>
  ```

# Mybatis动态SQL

- if标签: 实现动态拼接字符串

  test属性是必需的，当满足test中的条件时，if标签的内容就会被拼接

  ```xml
  <select id="selectUserByCondition" resultType="User">
      select * from t_user where 1=1
      <if test="username != null and username != ''">
          and username =#{username}
      </if>
      <if test="age != null and age != ''">
          and age = #{age}
      </if>
  </select>
  ```

- where标签

  - 当where标签中有内容时，会自动生成where关键字，并且将内容前的多余的and或or去掉（添加在后面的and不会被去掉：username=#{username} and）
  - 当where标签中没有内容时，where关键字不会被去掉。

  ```java
  <select id="selectUserByCondition" resultType="User">
      select * from t_user
      <where>
          <if test="username != null and username != ''">
              and username =#{username}
          </if>
          <if test="age != null and age != ''">
              and age = #{age}
          </if>
      </where>
  </select>
  ```

- trim标签

  属性：

  - prefix/suffix：向trim标签中前面或者后面添加指定内容
  - prefixOverrides/suffixOverrides：向trim标签中内容前面或者后面删除指定内容(比如and写在后面)

  ```java
  <select id="selectUserByCondition" resultType="User">
      select * from t_user
      <trim prefix="where" suffixOverrides="and|or">
          <if test="username != null and username != ''">
              username =#{username} and
          </if>
          <if test="age != null and age != ''">
              age = #{age}
          </if>
      </trim>
  </select>
  ```

- choose/when/otherwise标签（相当于if...else if...else）

  - choose标签放在where里，when和otherwise放在choose中使用

  - when标签至少有一个
  - otherwise标签至多有一个
  - 如果有多个when，那么一旦找到一个符合的when，后面的when就不会再找了，相当于找到了一个else if分支

  ```xml
  <select id="selectUserByCondition" resultType="User">
      select * from t_user
      <where>
          <choose>
              <when test="username != null and username != ''">
                  username = #{username}
              </when>
              <when test="age != null and age != ''">
                  age = #{age}
              </when>
              <otherwise>
                  id = 6
              </otherwise>
          </choose>
      </where>
  </select>
  ```

- foreach标签

  - collection:设置需要循环的数组或集合

  - item：表示数组或集合中的每一个元素

  - separator：循环体之间的分隔符

  - open：开始时添加的符号

  - close：结束时添加的符号

  - 通过数组实现批量删除

    ```xml
    <delete id="deleteUserByCondition">
        delete from t_user where id in
        <foreach collection="ids" item="id" separator="," open="(" close=")">
            #{id}
        </foreach>
    </delete>
    ```

    ```xml
    <delete id="deleteUserByCondition">
        delete from t_user where
        <foreach collection="ids" item="id" open="(" close=")" separator="or">
            id = #{id}
        </foreach>
    </delete>
    ```

  - 通过集合批量添加

    ```java
    <insert id="insertByList">
        insert into t_user values 
        <foreach collection="users" item="user" separator=",">
            (null ,#{user.username},#{user.password},#{user.age},#{user.sex},#{user.email})
        </foreach>
    </insert>
    ```

- sql标签

  当有些要查询的字段重复量大的时候，可以用sql标签单独定义出来，以后用的时候直接用include标签连接过去

  ```xml
  <sql id="mySql">id,username,age,sex,email</sql>
  <select id="findAllUser" resultMap="result">
      select <include refid="mySql"/> from t_user;
  </select>
  ```

