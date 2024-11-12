package com.example.CorporateEventer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PageController {


    @GetMapping("/index")
    public String index1() {
        return "index";
    }

    @GetMapping("/index.html")
    public String index2() {
        return "index";
    }

    @GetMapping("/register")
    public String registration() {
        return "register";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/user_page")
    public String userPageLoader() {
        return "user_page";
    }

    


}

