# NGiNX

- 今日任务：学习并对nginx有基本的了解

## nginx基本概念

1. **nginx定义**

   nginx是一个高性能的HTTP和反向代理的服务器，占有内存少，并发能力强。专为性能优化而开发的。

2. **反向代理**

   （1）正向代理

   ​	在浏览器配置代理服务器，通过代理服务器进行互联网访问

   （2）反向代理

   ​	客户端对代理无感知，客户端不需要任何配置，只需要从浏览器发送请求到反向代理服务器，由反向代理服务器去选择服务器获取数据，再返回给浏览器。

3. **负载均衡**

   单个服务器解决不了，增加服务器的数量，然后将请求分发到各个服务器上。将负载分发到不同服务器就是说的负载均衡

4. **动静分离**

   为了加快网站的解析速度，可以把动态页面和静态页面由不同服务器来解析，加快解析速度，降低单个服务器压力。

## 下载安装（Linux系统）

1. 安装pcre依赖
2. 安装zlib依赖
3. 安装openssl依赖
4. 安装nginx

安装完成后，usr文件夹下多出一个local/nginx，其中启动脚本在sbin目录中。

默认使用80端口，需要在防火墙中开启80端口



## nginx常用操作命令

- 使用命令前必须进入nginx的目录 /usr/local/nginx/sbin

查看nginx版本号

- ./nginx -v

关闭nginx

- ./nginx -s stop

启动nginx

- ./nginx

重新加载nginx(conf文件)

- ./nginx -s reload



## nginx配置文件

路径: usr/local/nginx/conf/nginx.conf

```bash
#创建进程的用户和用户组，多个时用空格隔开
#user  nobody;
#服务进程数量，一般等于 CPU 数量
worker_processes  1;
 
#全局错误日志定义，建议开启error级别日志.[ debug | info | notice | warn | error | crit ]
error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;
 
#记录进程 ID 的文件
#pid        logs/nginx.pid;
 
events {
	#一个 worker_processe 允许的最大并发连接数量
    worker_connections  1024;
}
 
http {
    include       mime.types;
    default_type  application/octet-stream;
 
    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';
 
    #access_log  logs/access.log  main;
 
    sendfile        on;
    #tcp_nopush     on;
 
    #keepalive_timeout  0;
    #http连接的持续时间
    keepalive_timeout  65;
 
	#gzip压缩设置，开启或关闭
    #gzip  on;
 
	#设定负载均衡的服务器列表，可以设置多个 upstream，使用不同的名称 wmx 区分即可
	#upstream 默认是没有，需要自己添加
	upstream wmx {
	    #weigth参数表示权值，权值越高被分配到的几率越大
	    server 127.0.0.1:8081 weight=5;
	    server 127.0.0.1:8082 weight=5;
	    server 127.0.0.1:8083 weight=5;
	}
 
    server {
    	#nginx监听的端口号
        listen       80;
        #当前 server（虚拟主机）的域名，可以有多个，用空格隔开
        server_name  localhost;
        #字符编码方式
        #charset koi8-r;
 
        #设定本虚拟主机的访问日志。关闭日志可以减少IO，提高性能。
        #access_log  logs/host.access.log  main;
 
        #默认请求
        location / {
            #定义服务器的默认网站根目录位置
            root   html;
            #定义首页索引文件的名称
            index  index.html index.htm;
 
            #请求转向 wmx 定义的服务器列表
            proxy_pass http://wmx
        }
 
        #error_page  404              /404.html;
 
        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
 
        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}
 
        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}
 
        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }
 
 
    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;
 
    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}
 
 
    # HTTPS server
    #
    #server {
    #    listen       443 ssl;
    #    server_name  localhost;
 
    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;
 
    #    ssl_session_cache    shared:SSL:1m;
    #    ssl_session_timeout  5m;
 
    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers  on;
 
    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}
}
```



> 三部分组成

第一部分：全局块

- 配置文件开始至events之间的内容
- 主要配置一些影响nginx服务器整体运行相关的指令

第二部分：events块

- 影响nginx服务器与用户的网络连接
- 比如：worker_connections  1024;表示最大连接数

第三部分：http块

- http块包括http全局块，server块





## 配置反向代理实例1

- 实现效果：打开浏览器输入www.123.com，跳转到tomcat主页面

当浏览器输入一个网站，浏览器会先去本地的host文件查看是否有映射的ip，如果没有再去DNS上找。所以这里我们需要将www.123.com和ip的映射添加到host文件中。

1. HOSTS文件添加

   ```bash
   192.168.17.129	www.123.com
   ```

2. 在nginx进行请求转发配置

   ```bash
   server {
       	#nginx监听的端口号
           listen       80;
           #当前 server（虚拟主机）的域名，可以有多个，用空格隔开
           server_name  192.168.17.129;
           #默认请求
           location / {
               #定义服务器的默认网站根目录位置
               root   html;
               #定义首页索引文件的名称
               index  index.html index.htm;
    
               #请求转向 wmx 定义的服务器列表
               proxy_pass http://127.0.0.1：8080;
           }
       }
   ```

   重新启动下nginx就可以访问了，在浏览器中输入www.123.com 跳转到tomcat首页

## 反向代理实例2

- 实现效果：根据访问路径跳转到不同端口的服务，nginx监听端口为9001

  - 访问http://192.168.17.129:9001/edu/直接跳转到127.0.0.1:8080
  - 访问http://192.168.17.129:9001/vod/直接跳转到127.0.0.1:8081

- 添加server规则

  ```bash
  server {
      listen       9001;
      server_name  192.168.17.129;
  
      location ~ /edu/ {
          proxy_pass http://127.0.0.1：8080;
      }
      
      location ~ /vod/ {
      	proxy_pass http://127.0.0.1：8081;
      }
  }
  ```

- 开放对外访问的端口号

  - 查看开放的端口号：firewall -cmd --list-all
  - 设置开放端口号：
    - firewall-cmd --add-service=http -permanent
    - firewall-cmd --add-port=80/tcp -permanent
  - 重启防火墙：firewall-cmd -reload

> location符号说明

```bash
=:用于不含正则表达式的uri前，严格匹配
~：用于表示uri包含正则表达式，区分大小写
~*：用于表示uri包含正则表达式，不区分大小写
^~：用于不含正则表达式的uri前，要求nginx服务器找到标识符uri和请求字符串匹配度最高的location后，立即用此location处理，不使用location块中正则uri和请求字符串做匹配
```



## 配置实例-负载均衡

- 实现效果：浏览器输入地址http://192.168.17.129/edu/a.html，负载均衡效果，平均分配到8080和8081端口中

> 配置

```bash
http {
	# 负载均衡的配置
	upstream myserver {
	    #weigth参数表示权值，权值越高被分配到的几率越大
	    server 192.168.17.129:8080 weight=6;
	    server 192.168.17.129:8081 weight=5;
	}
    server {
        listen       80;
        server_name  192.168.17.129;

        location / {
            proxy_pass http://myserver;
            
            root   html;
            index  index.html index.htm;
        }
    }
}
```



## 几种负载均衡分配的策略

- 轮询（默认）

- weight（权重策略）

- ip_hash：每个请求按访问ip的hash结果分配，这样每个访客固定访问一个后端服务器，可以解决session问题。

  ```bash
  # 负载均衡的配置
  upstream myserver {
  	ip_hash;
      server 192.168.17.129:8080;
      server 192.168.17.129:8081;
  }
  ```

- fair：根据后端服务器的响应时间来分配请求

  ```bash
  upstream myserver {
  	fair;
      server 192.168.17.129:8080;
      server 192.168.17.129:8081;
  }
  ```

  



## nginx配置实例-动静分离

1. 什么是动静分离？

   - 如果是动态请求，就去访问tomcat服务器
   - 如果是静态请求，去访问静态资源服务器

2. 为什么需要动静分离？

   为了效率

3. expires参数设置

   设置浏览器缓存过期时间，减少与服务器之间的请求和流量。给一个资源设置过期时间，不需要去服务器验证，直接通过浏览器自身确认是否过期。（不适合经常变动的资源）。比如设置3d，在这三天内访问这个url，发送一个请求，对比服务器该文件最后更新时间没有变化，则不会从服务器抓取，返回状态码304，如果有修改直接从服务器抓取，返回200.



## 高可用

- 引入：
  - 如果nginx宕机了？是不是就挂了，所以需要解决办法，再创建一个从nginx服务器。

> 高可用

- 当主nginx宕机了，自动切换到从nginx。那么从nginx变为主nginx。
- 需要对外提供虚拟ip，因为主nginx和从nginx的ip地址肯定不同，一旦主挂了，ip地址也变了，外界调用的ip并不会变，所以要提供虚拟ip，一旦有一个宕机了，另外一个也能正常使用。（需要keepalived）

==keepalived安装==

安装好之后，配置文件在/etc/keepalived/keepalived.conf。关于主从的nginx配置都在这里配置。



配置信息含义

主：

```bash
#全局配置
global_defs {
    router_id lb201 #服务器域名名字，可在/etc/hosts中查看127.0.0.1映射的域名
}

#脚本配置
vrrp_script chk_http_port {
    script "/usr/local/src/nginx_check.sh"#脚本路径
    interval 2 #（检测脚本执行的间隔）;每隔2s检查一次
    weight 2 #设置服务器权重
}

#虚拟ip的配置
vrrp_instance laowang {
        state MASTER # Master为Master；Salve为BACKUP
        interface ens33 #网卡；可以用ip addr 看网卡名
        virtual_router_id 51 # 主、备机的 virtual_router_id 必须相同
        priority 100 # 主、备机取不同的优先级，主机值较大，备份机值较小
        advert_int 1 #每1s发送一次心跳
        authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.73.250 // VRRP H 虚拟ip地址,多台keepalived绑定一个ip；必须同一网段
    }
}
```

从：

```bash
#全局配置
global_defs {
    router_id lb202 #服务器域名名字，可在/etc/hosts中查看127.0.0.1映射的域名
}

#脚本配置
vrrp_script chk_http_port {
    script "/usr/local/src/nginx_check.sh"#脚本路径
    interval 2 #（检测脚本执行的间隔）;每隔2s检查一次
    weight 2 #设置服务器权重
}

#虚拟ip的配置
vrrp_instance laowang {
        state BACKUP # Master为Master；Salve为BACKUP
        interface ens33 #网卡；可以用ip addr 看网卡名
        virtual_router_id 51 # 主、备机的 virtual_router_id 必须相同
        priority 90 # 主、备机取不同的优先级，主机值较大，备份机值较小
        advert_int 1 #每1s发送一次心跳
        authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.73.250 // VRRP H 虚拟ip地址,多台keepalived绑定一个ip；必须同一网段
    }
}
```

再配置一个脚本，用来检测主机的状态，是否宕机

```bash
#!/bin/bash
A=`ps -C nginx ĘCno-header |wc -l`
if [ $A -eq 0 ];then
    /usr/local/software/nginx/sbin/nginx  #nginx的安装路径
    sleep 2
    if [ `ps -C nginx --no-header |wc -l` -eq 0 ];then
        killall keepalived
    fi
fi
```



> 小结

对nigix有一个简单清晰的了解，效率高（负载均衡，动静分离），安全（使用反向代理）。nginx的一些配置文件的了解，几种负载均衡的调度策略。

