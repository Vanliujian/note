# Filter

## filter是什么

filter是用来拦截Servlet容器对Servlet的调用，在Servlet响应前后都会进行一些处理。

## 为什么需要filter

有很多的html，jsp，servlet执行前或者执行后需要执行同一段代码操作，为了解决这种情况可以使用过滤器技术。

同样如果需要判断一个用户是否登录，也可以使用filter进行判断，如果没有登录就让其返回登录页面。

## 它的应用场景有哪些

日志、过滤敏感字符、加密解密、编码、为原来的逻辑添加新功能

## 用处

在HttpServletRequest到达Servlet之前，拦截用户的HttpServletRequest，修改其请求头和数据

在HttpServletResponse到达客户端之前，拦截并可以修改请求头和数据

## Filter使用

### 创建Filter

1. 创建filter处理类

   必须实现javax.servlet下的Filter接口

   ```java
   package com.van.filter;
   
   import javax.servlet.*;
   import javax.servlet.http.HttpServletRequest;
   import javax.servlet.http.HttpServletResponse;
   import javax.servlet.http.HttpSession;
   import java.io.IOException;
   
   public class SecurityFilter implements Filter {
       private FilterConfig config;
       @Override
       public void init(FilterConfig filterConfig) throws ServletException {
           System.out.println("过滤器初始化");
           this.config = filterConfig;
       }
   
       //身份认证的过滤
       @Override
       public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
           ServletContext context = this.config.getServletContext();
           HttpServletRequest req = (HttpServletRequest) request;
           HttpServletResponse res = (HttpServletResponse) response;
           HttpSession session = req.getSession();
           if (session.getAttribute("username") != null) {
               //具体的查询数据库校验略过
               context.log("用户已经登录");
               //放行
               chain.doFilter(request,response);
           } else {
               context.log("未登录");
               //重定向到失败页面，或者登录页面,这里需要加上上下文路径
               res.sendRedirect("/spring_mvc_project/failure.jsp");
           }
       }
   
       @Override
       public void destroy() {
           this.config = null;
       }
   }
   ```

2. 在web.xml中配置filter

   ```xml
   <!--定义过滤器-->
   <filter>
       <filter-name>security</filter-name>
       <filter-class>com.van.filter.SecurityFilter</filter-class>
   </filter>
   <!--过滤器映射-->
   <filter-mapping>
       <filter-name>security</filter-name>
       <!--凡是要访问buy包下的资源，都需要映射-->
       <url-pattern>/buy/*</url-pattern>
   </filter-mapping>
   ```

### 过滤器链FilterChain

- 执行顺序

  会按照在web.xml中配置的顺序进行执行，先执行前面的doFilter之前的方法，再放行到下一个过滤器，执行doFilter之前的方法，经过doFilter放行回来后，依次执行doFilter后面的方法。

### HttpServletRequestWrapper

- 在request到达服务器前先处理一下，比如过滤敏感字符。

### HttpServletResponseWrapper

- 返回给客户端前可以对其修改

