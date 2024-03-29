# 02. Spring Security Login, Logout 설정

## 02-1. ViewResolver와 ViewSecurity 설정

/src/main/java/com/edutech/config/WebConfig.java 작성

```java
package com.edutech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //MainController 에서 @GetMapping("/")으로 설정하는 방법도 있지만,
        //WebMvcConfigurer 에서 addViewControllers 메소드에서 해당 뷰를 정할 수 있는 방법이 있음. 둘 중에 하나만 활용바람.
        registry.addViewController("/").setViewName("index");
    }


    //SpringSecurityDialect는 ThymeLeaf 에서 Spring Security를 활용하기 위한 내용을 설정할 수 있음.
    @Bean
    public SpringSecurityDialect securityDialect(){
        return new SpringSecurityDialect();
    }
}
```

<br>

## 02-2. Application 설정 정보에 admin 사용자 등록하기

/src/main/resources/application.properties 수정하기

```properties
server.port=8086

logging.level.org.springframework=info
logging.level.com.edutech=info

spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3306/edutech
spring.datasource.username=root
spring.datasource.password=1234

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# 아래는 admin 사용자를 등록한 내용입니다.
spring.security.user.name=admin
spring.security.user.password=1234
spring.security.user.roles=ADMIN,USER,TEACHER
```

<br>

## 02-3. Spring Security 연결 제어

/src/main/java/com/edutech/config/SecurityConfig.java 작성하기

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //@EnableGlobalMethodSecurity를 설정하게 되면, Controller의 해당 요청 메소드위에 @PreAuthorize 어노테이션을 설정하면, 특정 Role 권한을 가진 사람만 접근하도록 할 수 있음.
public class SecurityConfig {

    
    //등록 후 실행하면 로그인 창에 입력한 비밀번호가 암호화 되어야 있어야 하므로 암호화된 비밀번호를 입력하여야 로그인이 이루어진다.
    /*
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    */
    
    //등록 후 실행하면 로그인 창을 찾지 못하므로 view(jsp나 Thymeleaf)를 작성한 후 formLogin() 메소드로 반드시 로그인 창을 지정하여 설정하여야 한다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable());
        http
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll().requestMatchers(new AntPathRequestMatcher("/board/read")).hasRole("USER").anyRequest().authenticated());
        http.formLogin((formLogin) -> formLogin.loginPage("/member/login").loginProcessingUrl("/member/loginPro").usernameParameter("username").passwordParameter("password").defaultSuccessUrl("/").failureUrl("/member/login"));
        http.logout((logout) -> logout.logoutUrl("/member/logout").logoutSuccessUrl("/"));
        //http.logout(Customizer.withDefaults());
        return http.build();
    }
}
```

<br>

## 02-4. Member Controller 작성

/src/main/java/com/eduteth/controller/MemberController.java 작성하기

```java
package com.edutech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    @GetMapping("/member/login")
    public String loginPage(){
        return "member/login";
    }
}
```

<br>

## 02-5. 스타일 및 리소스 추가

/src/main/resources/static/css/signin.css 작성하기

```css
@charset "utf-8";
body {
    padding-top: 40px;
    padding-bottom: 40px;
    background-color: #eee;
}

.form-signin {
    max-width: 330px;
    padding: 15px;
    margin: 0 auto;
}
.form-signin .form-signin-heading,
.form-signin .checkbox {
    margin-bottom: 10px;
}
.form-signin .checkbox {
    font-weight: 400;
}
.form-signin .form-control {
    position: relative;
    box-sizing: border-box;
    height: auto;
    padding: 10px;
    font-size: 16px;
}
.form-signin .form-control:focus {
    z-index: 2;
}
.form-signin input[type="email"] {
    margin-bottom: -1px;
    border-bottom-right-radius: 0;
    border-bottom-left-radius: 0;
}
.form-signin input[type="password"] {
    margin-bottom: 10px;
    border-top-left-radius: 0;
    border-top-right-radius: 0;
}
```

<br>

## 02-6. 로그인 페이지 작성

/src/main/resources/templates/member/login.html 작성하기

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 창</title>
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M" crossorigin="anonymous">
    <link th:href="@{/css/styles.css}" rel="stylesheet" />
    <link th:href="@{/css/signin.css}" rel="stylesheet" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js"></script>
</head>
<body>
<form class="form-signin" method="post" action="/member/loginPro">
    <h2 class="form-signin-heading">Please sign in</h2>
    <p>
        <label for="username" class="sr-only">Username</label>
        <input type="text" id="username" name="username" class="form-control" placeholder="Username" required autofocus>
    </p>
    <p>
        <label for="password" class="sr-only">Password</label>
        <input type="password" id="password" name="password" class="form-control" placeholder="Password" required>
    </p>
    <input name="_csrf" type="hidden" value="uL_46kbIIND5giaTKUCtvsAZ0Kxz4D1mPWDhXj8qRcwuxRqIgY7N0ySrE7LU5EKrGG2Zi6Yg_ZVAhQ9LCFKHagsadfUYpi3r">
    <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
</form>
</body>
</html>
```

## 02-7. 메인 페이지 수정

/src/main/resources/templates/index.html 수정하기

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>메인</title>
</head>
<body>
    <h1>메인</h1>
    <hr>
    <h2 th:text="${title}"></h2>
    <h2 th:text="${msg}"></h2>
    <hr>
    <a th:href="@{/board}">게시판 목록</a>
    <div sec:authorize="isAnonymous()">
        <span style="color:deeppink;font-size:20px">로그인하지 않은 사람</span>
    </div>
    <div sec:authorize="isAuthenticated()">
        <div sec:authorize="hasRole('USER')">user권한일 경우</div>
        <div sec:authorize="hasRole('ADMIN')">admin권한일 경우</div>
        <div>
            <span sec:authorize="isAuthenticated()">로그인해서 인증을 받은 사람</span>
        </div>
        <div>인증받은 사람 :  <span sec:authentication="name"></span></div>
        <div>인증받은 사람의 권한 :  <span sec:authentication="principal.authorities"></span></div>
    </div>
</body>
</html>
```

<br>

## 02-8. 게시판 목록 페이지에 로그아웃 버튼 추가

/src/main/resources/templates/board/list.html 에 로그아웃 버튼 추가하기

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>글 목록</title>
    <link th:href="@{/css/styles.css}" rel="stylesheet" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js"></script>
</head>
<body>
<div class="content">
    <div class="row mt-3">
        <div class="col">
            <div class="card">
                <div class="card-header">
                    <!-- 로그인 중일 때만 로그아웃 버튼 추가 -->
                    <span sec:authorize="isAuthenticated()">
                        <a th:href="|@{/member/logout}|" class="btn btn-secondary">
                            Logout
                        </a>
                    </span>
                    <!-- 로그아웃 버튼 추가 끝 -->
                    <h2 class="title">글 목록</h2>
                </div>
                <div class="card-body" >
                    <h5 class="card-title">Board List </h5>

                    <table class="table">
                        <thead>
                        <tr>
                            <th scope="col">Bno</th>
                            <th scope="col">Title</th>
                            <th scope="col">Writer</th>
                            <th scope="col">RegDate</th>
                        </tr>
                        </thead>

                        <tbody>
                        <tr th:each="dto:${boardList}"  >
                            <td>[[${dto.bno}]]</td>
                            <td>
                                <a th:href="|@{/board/read(bno =${dto.bno})}|" class="text-decoration-none"> [[${dto.title}]] </a>
                            </td>
                            <td>[[${dto.writer}]]</td>
                            <td>[[${#temporals.format(dto.regDate, 'yyyy-MM-dd')}]]</td>
                        </tr>
                        </tbody>
                    </table>
                </div><!--end card body-->
                <div class="my-4">
                    <div class="float-end">
                        <a th:href="|@{/board/write}|" class="btn btn-secondary">
                            Insert
                        </a>
                    </div>
                </div>
            </div><!--end card-->
        </div><!-- end col-->
    </div><!-- end row-->
</div>
<script th:inline="javascript">
    const errors = [[${errors}]]
    console.log(errors)
    let errorMsg = ''
    if(errors){
        for (let i = 0; i < errors.length; i++) {
            errorMsg += `${errors[i].field}은(는) ${errors[i].code} \n`
        }
        alert(errorMsg)
    }

</script>
</body>
</html>
```

<br>

## 02-9. Board Controller 에 특정 글 상세보기할 경우 로그인한 사용자만 볼 수 있도록 설정

/src/main/java./com/edutech/controller/BoardController.java 에 @PreAuthorize("hasRole('USER')") 추가하기

```java
package com.edutech.controller;

import com.edutech.dto.BoardDTO;
import com.edutech.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;

    @GetMapping({"/board", "/board/list"})
    public String boardListAll(Model model){
        List<BoardDTO> boardList = boardService.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    //해당 글 상세보기를 요청할 경우 USER Role이 있는 경우 즉, 로그인 중인 경우만 해당 정보를 볼 수 있도록 유도함.
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/board/read")
    public String boardFindByBno(Integer bno, Model model){
        BoardDTO board = boardService.findByBno(bno);
        model.addAttribute("dto", board);
        return "board/read";
    }

    @GetMapping("/board/write")
    public String boardForm(){
        return "board/write";
    }

    @PostMapping("/board/register")
    public String boardWrite(@Valid BoardDTO boardDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        log.info("board POST register.......");
        if(bindingResult.hasErrors()) {
            log.info("has errors.......");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors() );
        }
        Integer bno  = boardService.register(boardDTO);
        return "redirect:/board/list";
    }

    @GetMapping("/board/modify")
    public String boardModify(Integer bno, Model model){
        BoardDTO boardDTO = boardService.findByBno(bno);
        model.addAttribute("dto", boardDTO);
        return "board/modify";
    }

    @PostMapping("/board/modify")
    public BoardDTO boardModifyPro(@Valid BoardDTO boardDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes){
        log.info("board modify post......." + boardDTO);
        if(bindingResult.hasErrors()) {
            log.info("has errors.......");
            String link = "bno="+boardDTO.getBno();
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors() );
            redirectAttributes.addAttribute("bno", boardDTO.getBno());
            return boardDTO;
        }
        boardService.modify(boardDTO);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("bno", boardDTO.getBno());
        return boardDTO;
    }

    @PostMapping("/board/remove")
    public String boardRemove(Integer bno, RedirectAttributes redirectAttributes) {
        log.info("remove post.. " + bno);
        boardService.remove(bno);
        redirectAttributes.addFlashAttribute("result", "removed");
        return "redirect:/board/list";
    }
}
```


