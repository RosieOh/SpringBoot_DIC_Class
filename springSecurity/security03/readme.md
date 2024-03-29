# 03. Spring Security 회원가입, 이메일 중복 체크

## 03-1. Application 설정 정보에 임시 사용자 정보 제거

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
```

<br>

## 03-2. Spring Security 에서 가입시 암호화 Bean 등록하기

/src/main/java/com/edutech/config/SecurityConfig.java 수정하기

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
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    //등록 후 실행하면 로그인 창을 찾지 못하므로 view(jsp나 Thymeleaf)를 작성한 후 formLogin() 메소드로 반드시 로그인 창을 지정하여 설정하여야 한다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //csrf 비활성화
        http.csrf((csrf) -> csrf.disable());
        http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll());
        //로그인 설정
        http.formLogin((formLogin) -> formLogin
                .loginPage("/member/login")
                .loginProcessingUrl("/member/loginPro")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/")
                .failureUrl("/member/login"));
        //로그아웃 설정
        http.logout((logout) -> logout
                .logoutUrl("/member/logout")
                .logoutSuccessUrl("/"));
        return http.build();
    }
}
```

<br>

## 03-3. Member의 Role 만들기

/src/main/java/com/edutech/dto/Role.java 작성하기

```java
package com.edutech.dto;

public enum Role {
    USER, TEACHER, ADMIN
}
```

<br>

## 03-4. Member Entity 만들기

/src/main/java/com/edutech/entity/Member.java 작성하기

```java
package com.edutech.entity;

import com.edutech.dto.Role;
import com.edutech.dto.MemberFormDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.*;

@Entity
@Table(name="member")
@Getter @Setter
@ToString
public class Member {

    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.USER);
        return member;
    }
}
```

<br>

## 03-5. Member 가입 창의 폼 검증 클래스 MemberFormDto 작성

/src/main/java/com/edutech/dto/MemberFormDto.java 작성하기

```java
package com.edutech.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberFormDto {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식으로 입력해주세요.")
    private String email;

    @NotEmpty(message = "비밀번호는 필수 입력 값입니다.")
    @Length(min=8, max=16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요")
    private String password;

    @NotEmpty(message = "주소는 필수 입력 값입니다.")
    private String address;
}

```

<br>

## 03-6. Member Repository 작성

/src/main/java/com/edutech/repository/MemberRepository.java 작성하기

```java
package com.edutech.repository;

import com.edutech.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
}
```

<br>

## 03-7. Member Servive 작성

/src/main/java/com/edutech/service/MemberService.java 작성하기

```java
package com.edutech.service;

import com.edutech.entity.Member;
import com.edutech.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public Member saveMember(Member member){
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    public void validateDuplicateMember(Member member){
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    public boolean memberDupValidation(String email){
        Member findMember = memberRepository.findByEmail(email);
        if(findMember != null){
            return false;
        } else {
            return true;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);
        if(member == null){
            throw new UsernameNotFoundException(email);
        }
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }
}
```

<br>

## 03-8. Member Controller 작성

/src/main/java/com/edutech/controller/MemberController.java 작성하기

```java
package com.edutech.controller;

import com.edutech.dto.MemberFormDto;
import com.edutech.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.edutech.entity.Member;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping(value = "/new")
    public String memberForm(Model model){
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/join";
    }

    @PostMapping(value = "/joinPro")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "member/join";
        }
        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "member/join";
        }
        return "redirect:/";
    }

    @PostMapping(value = "/dup")
    @ResponseBody
    public boolean memberDupValidation(@RequestBody MemberFormDto data ){
        boolean result = memberService.memberDupValidation(data.getEmail());
        return result;
    }

    @GetMapping(value = "/login")
    public String loginMember(){
        return "member/login";
    }

    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "member/login";
    }

}
```

<br>

## 03-9. 로그인 페이지 수정

/src/main/resources/templates/member/login.html 수정하기

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인</title>
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/styles.css}" rel="stylesheet" />
    <link th:href="@{/css/signin.css}" rel="stylesheet" />
    <script src="https://code.jquery.com/jquery-latest.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js"></script>
    <style>
        .fieldError {
            color: #bd2130;
        }
    </style>
    <script>
        $(document).ready(function(){
            var errorMessage = [[${errorMessage}]];
            if(errorMessage != null){
                alert(errorMessage);
            }
        });
    </script>
</head>
<body>
<div class="container">
    <div class="content" style="width:1200px;margin:20px auto;">
        <form role="form" method="post" action="/member/loginPro">
            <div class="form-group">
                <label th:for="email">이메일주소</label>
                <input type="email" name="email" class="form-control" placeholder="이메일을 입력해주세요">
            </div>
            <div class="form-group">
                <label th:for="password">비밀번호</label>
                <input type="password" name="password" id="password" class="form-control" placeholder="비밀번호 입력">
            </div>
            <p th:if="${loginErrorMsg}" class="error" th:text="${loginErrorMsg}"></p>
            <button class="btn btn-primary">로그인</button>
            <button type="button" class="btn btn-primary" onClick="location.href='/members/new'">회원가입</button>
            <!--        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">-->
        </form>
    </div>
</div>
</body>
</html>
```

<br>

## 03-10. 회원 가입 페이지 작성

/src/main/resources/templates/member/join.html 작성하기

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입</title>
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/styles.css}" rel="stylesheet" />
    <link th:href="@{/css/signin.css}" rel="stylesheet" />
    <script src="https://code.jquery.com/jquery-latest.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js"></script>
    <style>
        .fieldError {
            color: #bd2130;
        }
    </style>
    <script>
        $(document).ready(function(){
            var errorMessage = [[${errorMessage}]];
            if(errorMessage != null){
                alert(errorMessage);
            }
        });
    </script>
</head>
<body>
<div class="container">
    <h2 class="form-signin-heading">회원 가입</h2>
    <form action="/member/joinPro" role="form" method="post" th:object="${memberFormDto}">
        <div class="form-group">
            <label th:for="name">이름</label>
            <input type="text" th:field="*{name}" class="form-control" placeholder="이름을 입력해주세요">
            <p th:if="${#fields.hasErrors('name')}" th:errors="*{name}" class="fieldError">Incorrect data</p>
        </div>
        <div class="form-group">
            <label th:for="email">이메일주소</label>
            <input type="email" th:field="*{email}" id="email" class="form-control" placeholder="이메일을 입력해주세요">
            <button type="button" class="btn btn-primary" onclick="validateDuplicate()">이메일 중복 검사</button>
            <p th:if="${#fields.hasErrors('email')}" th:errors="*{email}" class="fieldError">Incorrect data</p>
        </div>
        <div class="form-group">
            <label th:for="password">비밀번호</label>
            <input type="password" th:field="*{password}" class="form-control" placeholder="비밀번호 입력">
            <p th:if="${#fields.hasErrors('password')}" th:errors="*{password}" class="fieldError">Incorrect data</p>
        </div>
        <div class="form-group">
            <label th:for="address">주소</label>
            <input type="text" th:field="*{address}" class="form-control" placeholder="주소를 입력해주세요">
            <p th:if="${#fields.hasErrors('address')}" th:errors="*{address}" class="fieldError">Incorrect data</p>
        </div>
        <div>
            <button type="submit" id="reg-btn" class="btn btn-primary" style="">회원가입</button>
        </div>
        <!--        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">-->
    </form>
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
    <script>
        var regBtn = document.getElementById("reg-btn");
        regBtn.style.display='none';
        var email = document.getElementById("email");
        async function validateDuplicate() {
            let data = { email:email.value };
            let result = await axios.post('/member/dup', data);
            console.log(result.data);
            if(result.data){
                regBtn.style.display='inline-block';
            } else {
                regBtn.style.display='none';
            }
        }
    </script>
</div>
</body>
</html>
```

<br>

## 03-11. 메인 페이지 수정 - 회원가입 페이지 링크 추가

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
        <p>
            <a th:href="@{/member/new}">회원가입</a>
            <a th:href="@{/member/login}">로그인</a>
        </p>
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
