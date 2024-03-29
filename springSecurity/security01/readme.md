# 01. Spring Security 라이브러리 추가

<br>

## 처음 등록된 라이브러리
- spring boot 3.1.x
- spring Web
- spring Boot DevTools
- Validation
- Spring Data JPA
- MariaDB Driver
- Spring Security
- Thymeleaf
- Lombok

<br>

## 추가할 라이브러리
- ModelMapper 3.1.x
- QueryDSL JPA 5.0.0
- QueryDSL APT 5.0.0
- ThymeLeaf 3.1.x
- Thymeleaf Layout Dialect 3.1.x
- Thymeleaf extras springsecurity6 3.1.x
- Jakarta Persistence-api 3.1.0
- jakarta Annotation-api 2.1.1

<br>

## 01-1. build.gradle 수정

```groovy
plugins {
	id 'java'
	id 'war'
	id 'org.springframework.boot' version '3.1.6'
	id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com'
version = '0.0.1-SNAPSHOT'

java {
	sourceCompatibility = '17'
}

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	implementation 'org.modelmapper:modelmapper:3.1.0'
	implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.1.0'
	implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'			//QueryDSL 추가1
	implementation 'com.querydsl:querydsl-apt:5.0.0:jakarta'			//QueryDSL 추가2
	implementation 'org.springframework.boot:spring-boot-starter-security'	//Spring Security 추가1 - 기본 spring security 활성화
	implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'	//Spring Security 추가2 - thymeleaf에서 <div sec:> 로 시작하는 태그를 활성화
	testImplementation 'org.projectlombok:lombok:1.18.26'
	compileOnly 'org.projectlombok:lombok'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
	annotationProcessor 'org.projectlombok:lombok'
	annotationProcessor 'jakarta.persistence:jakarta.persistence-api:3.1.0'		//QueryDSL 추가3
	annotationProcessor 'jakarta.annotation:jakarta.annotation-api:2.1.1'		//QueryDSL 추가4
	annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'			//QueryDSL 추가5
	providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('bootBuildImage') {
	builder = 'paketobuildpacks/builder-jammy-base:latest'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

<br>

## 01-2. application.properties 설정 정보 추가

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

## 01-3. MainController 작성

/src/main/java/com/edutech/MainController.java 작성

```java
package com.edutech;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("title", "에듀테크");
        model.addAttribute("msg","스프링 부트 프로젝트입니다.");
        return "index";
    }
}
```


## 01-4. index.html 작성

/src/main/resources/templates/index.html를 thymeleaf 작성

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
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
</body>
</html>
```

## 01-5. Application 실행과 로그인 창에 입력할 정보

IDE에서 실행시 콘솔 창에 나타난 Using generated security password의 내용이 기본 비밀번호이며, 기본 사용자 아이디는 user이다.

```console
default User : user  --> id
Using generated security password: 2f636239-b9df-4c95-9014-e3dac9317c77 --> pw
```

<br>
<hr>
<br>

# 02. Spring Security Login, Logout 설정