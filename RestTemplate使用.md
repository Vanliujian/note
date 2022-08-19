# RestTemplate使用

RestTemplate是基于底层做的一个模板方法，Spring自带的。

默认使用HttpUrlConnection。

有三个执行引擎

- HttpClient
- Netty
- OkHttp

如果想切换引擎，可以在创建RestTemplate的时候通过构造方法传入他们的具体实现类。

> SpringBoot集成RestTemplate

直接在配置类中注册为Bean即可。

> Http协议状态码

1xx：通知

2xx：请求成功返回

3xx：重定向

4xx：客户端异常

5xx：服务端异常





package com.van.controller;

import com.van.bean.User;
import com.van.bean.VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class UserController {

```java
/**
 * 最简单的get方法
 * new RestTemplate().getForObject("http://localhost:8080/test", String.class)
 */
@RequestMapping("/test")
public String test1() {
    return "test1";
}

/**
 * 返回类型为java bean
 * 1. String respStr = new RestTemplate().getForObject("http://localhost:8080/getUser", String.class);
 * 2. User user = new RestTemplate().getForObject("http://localhost:8080/getUser", User.class);
 */
@RequestMapping("/getUser")
public User getUser() {
    return new User(1,"vae","123");
}

/**
 * 带参数的get方法
 * new RestTemplate().getForObject("http://localhost:8080/test2?username={a}&password={b}", String.class, "hello", "world")
 */
@RequestMapping("/test2")
public User test2(@RequestParam("username") String username, @RequestParam("password") String password) {
    return new User(1,username, password);
}

/**
 * 带header的get方法
 * HttpHeaders headers = new HttpHeaders();
 * headers.add("token", "!@#$%");
 * HttpEntity<?> entity = new HttpEntity<>(headers);
 * ResponseEntity<String> responseEntity = new RestTemplate().exchange("http://localhost:8080/test3?username={a}&password={b}",
 *         HttpMethod.GET, entity, String.class, "hello", "world");
 * System.out.println(responseEntity.getBody());
 */
@RequestMapping("/test3")
public User test3(HttpServletRequest request,  @RequestParam("username") String username, @RequestParam("password") String password) {
    String token = request.getHeader("token");
    return new User(1,username, password + "_" + token);
}

/**
 * HttpHeaders headers = new HttpHeaders();
 * headers.add("token", "!@#$%");
 * headers.add("Cookie", "token2=cooool;token3=baaaad");// 单单对于http请求来说，Cookie只是一个普通的Header
 * HttpEntity<?> entity = new HttpEntity<>(headers);
 * ResponseEntity<String> responseEntity = new RestTemplate().exchange("http://localhost:8080/test4?username={a}&password={b}",
 *      HttpMethod.GET, entity, String.class, "hello", "world");
 * System.out.println(responseEntity.getBody());
 */
@RequestMapping("/test4")
public User test4(HttpServletRequest request,  @RequestParam("username") String username, @RequestParam("password") String password) {
    String token = request.getHeader("token");
    String token2 = getCookie(request, "token2");
    String token3 = getCookie(request, "token3");
    return new User(1,username, password + "_" + token + "_" + token2 + "_" + token3);
}
private String getCookie(HttpServletRequest request, String cookieKey){
    return Arrays.stream(request.getCookies()).filter(e -> e.getName().equals(cookieKey))
            .findAny().orElseThrow(() -> new RuntimeException("missing cookie " + cookieKey))
            .getValue();
}
```

