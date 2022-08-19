- 今日任务

  查看公司代码，学习中间件的操作

# 查看公司代码

# ShardingSphere

官网：https://shardingsphere.apache.org/document/current/cn/overview/

1. 什么是ShardingSphere

   数据库中间件，由三个产品组成Sharding-JDBC、Sharding-Proxy，用于分库分表

2. 什么是分库分表

   由于数据量太大，放在一个表中对性能影响很大，所以需要分库分表

## 分库分表的方式

### 垂直切分

- 垂直分表

  把一部分字段数据放在另外一张表中（把字段拆分）

- 垂直分库

  把单一数据库按照业务进行划分，专库专表

### 水平切分

- 水平分表

- 水平分库

  创建多个相同的数据库，比如id为奇数放数据库A，偶数放数据库B

## 分库分表应用和问题

1. 应用
   - 在数据库设计时，先考虑垂直分库分表
   - 不要马上考虑水平分库分表，首先考虑缓存处理，读写分离，使用索引等，如果这些都不行，再水平分库分表。
2. 问题
   - 跨节点连接查询问题（分页，排序）
   - 多数据源管理问题

## Sharding-JDBC

- 轻量级Java框架

<img src="https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid-architecture_v2.png" alt="img" style="zoom:80%;" />

ShardingJDBC不是做分库分表，是简化操作多个数据库和表，主要做两个功能，数据分片，读写分离

### 数据源配置

https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/data-source/

1. 使用本地数据源

   ```properties
   spring.shardingsphere.datasource.names= # 真实数据源名称，多个数据源用逗号区分
   
   # <actual-data-source-name> 表示真实数据源名称
   spring.shardingsphere.datasource.<actual-data-source-name>.type= # 数据库连接池全类名
   spring.shardingsphere.datasource.<actual-data-source-name>.driver-class-name= # 数据库驱动类名，以数据库连接池自身配置为准
   spring.shardingsphere.datasource.<actual-data-source-name>.jdbc-url= # 数据库 URL 连接，以数据库连接池自身配置为准
   spring.shardingsphere.datasource.<actual-data-source-name>.username= # 数据库用户名，以数据库连接池自身配置为准
   spring.shardingsphere.datasource.<actual-data-source-name>.password= # 数据库密码，以数据库连接池自身配置为准
   spring.shardingsphere.datasource.<actual-data-source-name>.<xxx>= # ... 数据库连接池的其它属性
   ```

2. 引入maven依赖

   ```xml
   <dependency>
       <groupId>org.apache.shardingsphere</groupId>
       <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
       <version>${latest.release.version}</version>
   </dependency>
   ```

3. 数据分片

   ```properties
   spring.shardingsphere.datasource.names= # 省略数据源配置
   
   # 标准分片表配置
   spring.shardingsphere.rules.sharding.tables.<table-name>.actual-data-nodes= # 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持 inline 表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况
   
   # 分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一
   
   # 用于单分片键的标准分片场景
   spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.standard.sharding-column= # 分片列名称
   spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.standard.sharding-algorithm-name= # 分片算法名称
   
   # 用于多分片键的复合分片场景
   spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.complex.sharding-columns= # 分片列名称，多个列以逗号分隔
   spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.complex.sharding-algorithm-name= # 分片算法名称
   
   # 用于 Hint 的分片策略
   spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.hint.sharding-algorithm-name= # 分片算法名称
   
   # 分表策略，同分库策略
   spring.shardingsphere.rules.sharding.tables.<table-name>.table-strategy.xxx= # 省略
   
   # 自动分片表配置
   spring.shardingsphere.rules.sharding.auto-tables.<auto-table-name>.actual-data-sources= # 数据源名
   
   spring.shardingsphere.rules.sharding.auto-tables.<auto-table-name>.sharding-strategy.standard.sharding-column= # 分片列名称
   spring.shardingsphere.rules.sharding.auto-tables.<auto-table-name>.sharding-strategy.standard.sharding-algorithm-name= # 自动分片算法名称
   
   # 分布式序列策略配置
   spring.shardingsphere.rules.sharding.tables.<table-name>.key-generate-strategy.column= # 分布式序列列名称
   spring.shardingsphere.rules.sharding.tables.<table-name>.key-generate-strategy.key-generator-name= # 分布式序列算法名称
   
   spring.shardingsphere.rules.sharding.binding-tables[0]= # 绑定表规则列表
   spring.shardingsphere.rules.sharding.binding-tables[1]= # 绑定表规则列表
   spring.shardingsphere.rules.sharding.binding-tables[x]= # 绑定表规则列表
   
   spring.shardingsphere.rules.sharding.broadcast-tables[0]= # 广播表规则列表
   spring.shardingsphere.rules.sharding.broadcast-tables[1]= # 广播表规则列表
   spring.shardingsphere.rules.sharding.broadcast-tables[x]= # 广播表规则列表
   
   spring.shardingsphere.rules.sharding.default-database-strategy.xxx= # 默认数据库分片策略
   spring.shardingsphere.rules.sharding.default-table-strategy.xxx= # 默认表分片策略
   spring.shardingsphere.rules.sharding.default-key-generate-strategy.xxx= # 默认分布式序列策略
   spring.shardingsphere.rules.sharding.default-sharding-column= # 默认分片列名称
   
   # 分片算法配置
   spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.type= # 分片算法类型
   spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.props.xxx= # 分片算法属性配置
   
   # 分布式序列算法配置
   spring.shardingsphere.rules.sharding.key-generators.<key-generate-algorithm-name>.type= # 分布式序列算法类型
   spring.shardingsphere.rules.sharding.key-generators.<key-generate-algorithm-name>.props.xxx= # 分布式序列算法属性配置
   ```

写代码的时候还是照常写，只是配置的时候添加了一些配置。