# Maven

依赖管理：maven工程对jar包管理，将jar包放着仓库中。

以pom.xml文件中dependency属性管理依赖的jar包

maven工程 放置的是jar包

> 仓库

1. 本地仓库
2. 远程仓库
3. 中央仓库

仓库路径：在settings.xml中，有一个配置

Default：${user.home}/.m2/repository     //默认是这里

但是可以使用配置更改：<localRepository>/path/to/local/repo</localRepository>里面填的是要被更改的位置。

> Maven标准目录结构

src/main/java  核心代码部分

src/main/resources  配置文件部分

src/test/java目录  测试代码部分

src/test/resouces  测试配置文件

src/main/webapp 页面资源，js，css，图片等。



