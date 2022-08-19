> 核心配置类mybatis-config.xml

```xml
<!--
    必须按照这个顺序进行设置
    properties?,settings?,typeAliases?,typeHandlers?,objectFactory?,
    objectWrapperFactory?,reflectorFactory?,plugins?,environments?,
    databaseIdProvider?,mappers?
-->
<!--设置全限定类名的别名,可以在映射文件中使用-->
<typeAliases>
    <!--这个alias可以不写,默认是类名且不区分大小写-->
    <typeAlias type="com.van.pojo.User" alias="User"/>
    <!--package属性可以让一个包下的所有类都设置别名,全限定类名使用类名就行-->
    <package name="com.van.pojo"/>
</typeAliases>
```



> 映射文件

```xml
<!--这里的resultType使用类名即可-->
<select id="selectAllUser" resultType="User">
	select * from t_user;
</select>
```

