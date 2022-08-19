# Docker

- 今日任务：Docker学习

- 定义
  - 开源的应用容器引擎
- 为什么需要docker？
  - 开发软件一般有三个环境，生产环境，测试环境，上线环境，比如在生产环境上使用jdk8，测试环境使用jdk7，那么他们就会因为不兼容而发生错误，docker就是为了解决环境的不同而出现的。

## 安装docker

```shell
# yum包更新到最新版本
yum update
# 安装需要的软件包，yum-util提供yum-config-manager功能，另外两个是devicemapper驱动依赖的。
yum install -y yum-utils device-mapper-persistent-data lvm2
# 设置yum源
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
# 安装docker
yum install -y docker-ce
```

## docker架构

<img src="https://s2.loli.net/2022/08/05/2ERjIa4AqXnNr3b.jpg" alt="3b292df5e0fe9925352c266b0d1303d78db17128.jpg" style="zoom:80%;" /> 

三大组件

- 镜像

- 容器

- 仓库



## Docker命令

1. docker服务相关命令

   - 启动docker：systemctl start docker
   - 停止docker：systemctl stop docker
   - 重启docker：systemctl restart docker
   - 查看docker状态：systemctl status docker
   - 设置开机启动docker：systemctl enable docker

2. 镜像相关命令（hub.docker.com去查找docker可以下载的哪些镜像）

   - 查看镜像：docker images

   - 查看所有镜像id：docker images -q

   - 搜索镜像：docker search 镜像名

   - 拉取镜像：docker pull 镜像名称(默认下latest版本，如果要具体版本号，可以去hub.docker.com查看)

   - 删除镜像：docker rmi 镜像id

   - ```bash
     删除本地所有镜像：docker rmi `docker images -q`
     ```

3. 容器相关命令

   - 查看正在运行的容器：docker ps
   - 查看所有容器：docker ps -a
   - 创建并启动容器：docker run 参数
     - -i：保持容器运行。通常和-t一起使用。使用-it后，容器创建后自动进入容器，退出容器后，容器自动关闭
     - -t：为容器重新分配一个伪输入终端，通常和-i一起使用
     - -d：以守护模式运行，创建一个容器在后台运行，需要使用docker exec进入容器。退出后，容器不会关闭。
     - -it：创建的容器一般为交互式容器，-id创建的容器一般为守护式容器
     - --name：为创建的容器命名
   - 进入容器：docker exec 参数
   - 停止容器：docker stop 容器名称
   - 启动容器：docker start 容器名称
   - 删除容器：docker rm 容器名称
   - 查看容器信息：docker inspect 容器名称

## docker容器的数据卷

引入：

- Dcoker容器删除后，容器中的数据会随之删除吗？（会）
- Docker容器和外部机器可以直接交换文件吗？（不可以）
- 容器之间想要数据交互？

为了解决上述问题，数据卷出现了

数据卷概念：

- 数据卷是宿主机中的一个目录或文件
- 当容器中的数据和数据卷目录绑定，两者的修改会立即同步
- 一个数据卷可以被多个容器同时挂载
- 一个容器也可以被挂载多个数据卷

数据卷作用：

- 容器数据持久化
- 外部机器和容器的间接通信
- 容器之间数据交换

配置数据卷：

- 创建容器时，使用-v参数设置数据卷

docker run ... -v宿主机目录（文件）：容器内目录（文件）...

- 目录必须是绝对路径
- 如果目录不存在会自动创建
- 可以挂载多个数据卷

配置数据卷容器：创建一个容器，挂载一个目录，让其它容器继承自该容器（--volume-from）

1. 创建启动c3数据卷容器，使用-v参数设置数据卷

   docker run -it --name=c3 -v /volume centos:7 /bin/bash（/bin/bash可以不写）

2. 创建启动c1、c2容器，使用--volumes-from参数设置数据卷

   docker run -it --name=c1 --volumes-from c3 centos:7 /bin/bash

   docker run -it --name=c2 --volumes-from c3 centos:7 /bin/bash

## docker应用部署

### MySQL部署

端口映射：由于外部机器不能直接访问宿主机的容器，可以访问宿主机，再由宿主机访问容器，把宿主机的某一个端口映射到容器的某一个端口叫端口映射。

1. 创建mysql目录

2. 创建容器

   ```shell
   docker run -id \
   -p 3307:3306 \
   --name=c_mysql \
   -v $pwd/conf:etc/mysql/conf.d \
   -v $pwd/logs:/logs \
   -v $pwd/data:/var/lib/mysql \
   -e MYSQL_ROOT_PASSWORD=123 \      #初始化root用户密码
   mysql:8.0.20
   ```



### Tomcat部署

搜索并拉取tomcat镜像

创建容器，设置端口和目录映射

```shell
docker run -id --name=c_tomcat \
-p 8080:8080 \
-v $pwd:/usr/local/tomcat/webapps \
tomcat
```



### nginx部署

其中nginx.conf需要提前准备好

```shell
docker run -id --name=c_nginx \
-p 80:80 \
-v $pwd/conf/nginx.conf:/etc/nginx/nginx.conf \
-v $pwd/logs:/var/log/nginx \
-v $pwd/html:/usr/shars/nginx.html \
nginx
```



### redis部署

创建容器，设置端口映射

docker run -id --name=c_redis -p 6379:6379 redis:5.0

使用外部机器连接redis

redis-cli.exe -h 192.168.149.135 -p 6379



## Dockfile

### Docker镜像原理

- docker镜像本质？
  - 一个分层文件系统
- docker中一个centos镜像为什么只要200MB，而一个centos操作系统的iso要几个G
  - 存在复用
- docker中一个tomcat镜像为什么有500MB，而一个tomcat安装包只有70多MB
  - 由于docker镜像是分层的，tomcat虽然只有70多MB，但是它需要依赖父镜像和基础镜像。



Linux文件管理系统由bootfs和rootfs两部分组成

- bootfs：包含bootloader（引导加载程序）和kernel（内核）
- rootfs：root文件系统，包含的就是典型Linux系统中的/dev，/proc，/bin，/etc等标准目录和文件
- 不同的Linux发行版，bootfs基本一样，rootfs不同

Docker镜像是由特殊的文件系统叠加而成，最低端是bootfs，使用宿主机的bootfs，第二层是root文件系统rootfs，称为base image，再往上可以叠加其它镜像文件。

### 镜像制作

- 目录挂载过的是不会被制作到镜像中

1. 容器转换为镜像

   docker commit 容器id 镜像名称:版本号

   镜像转换为压缩文件docker save -o 压缩文件名称 镜像名称:版本号

   压缩文件还原成镜像：docker load -i 压缩文件名称

### dockfile概念

- 是一个文本文件，包含了一些命令

具体命令见官方文档：https://docs.docker.com/engine/reference/builder/#dockerignore-file

FROM：指定基础镜像

LABEL：指定镜像元数据

RUN：执行shell命令

COPY：复制文本

ADD：复制和解包文件

CMD：容器启动命令

ENV：设置环境变量

VOLUME：挂载点

EXPOSE：暴露端口

WORKDIR：指定工作目录

```shell
from java:8
add springboot-hello-snapshot.jar app.jar
cmd java -jar app.jar
```

docker build -f ./springboot_dockerfile -t app .

docker run -id -p 9090:8080 app



## Docker服务编排

1. 为什么需要服务编排

   微服务架构系统中一般包含若干个微服务，每个微服务部署多个实例，每个微服务都要手动停止的话，工作量会很大。

2. 服务编排：按照一定的业务规则批量管理容器

3. 使用

   1. 利用dockerfile定义运行环境镜像
   2. 使用docker-compose.yml定义组成应用的各服务
   3. 运行docker-compose up启动应用（让容器按顺序启动）

## Docker私有仓库

拉取私有仓库

- docker pull registry

启动私有仓库容器

- docker run -id --name=registry -p 5000:5000 registry

访问：浏览器输入http://私有仓库服务器ip:5000/v2/_catalog

修改daemon.json：vim /etc/docker/daemon.json，添加一个key保存退出。让docker信任私有仓库地址。{"insecure-registries":["私有仓库服务器ip:5000"]}

重启docker服务，再启动容器

`将镜像上传至私有仓库`

1. 标记镜像为私有仓库的镜像

   docker tag centos:7 私有仓库服务器IP:5000/centos:7

2. 上传标记的镜像

   docker push 私有仓库服务器IP:5000/centos:7

`将镜像拉取到本地`

docker pull 私有仓库服务器IP:5000/centos:7



## Docker相关概念

- docker容器虚拟化与传统虚拟机比较

  相同：

  - 容器和虚拟机具有相似的资源隔离和分配优势

  不同：

  - 容器虚拟的是操作系统，虚拟机虚拟的是硬件
  - 传统虚拟机可以运行不同操作系统，容器只能运行同一种操作系统



> 小结

了解Docker架构，掌握一些简单的Docker命令，学习docker数据卷，利用docker来部署mysql、tomcat、nginx、redis。了解使用Docker搭建私有仓库。学习Dockerfile来制作镜像。

