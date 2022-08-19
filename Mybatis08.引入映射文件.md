rescources中创建多层的包通过/来分割



# 引入映射文件

> 正常引入是这样的

```xml
<mappers>
	<mapper resource="mappers/UserMapper.xml"/>
</mappers>
```



> 通过包来引入整个包中的映射文件

要求 :

1. 包名要和接口的包名一致(全限定类名一致)
2. 映射文件的名称要和接口名一致

