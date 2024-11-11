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
    


}

