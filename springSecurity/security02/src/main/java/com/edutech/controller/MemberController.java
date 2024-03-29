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
