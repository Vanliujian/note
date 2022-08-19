# PowerDesign

设计表结构的软件

# Apollo

主流的配置中心：

- Disconf
- Spring Cloud Config
- Apollo
- Nacos

获取数据时用到的参数

- -Dapp.id=应用名称

- -Denv=环境名称

- -Dapollo.cluster=集群名称

- -D环境_meta=meta地址

> 搭建本地Apollo

有三个组件：

- apollo-adminservice
  - 用于对管理界面提供支持的管理服务。
- apollo-config-service
  - 分布式配置中心核心，提供配置的更新、客户端推拉功能。
- apollo-portal-service
  - 管理界面的权限、用户相关服务，拥有自己的数据库。















