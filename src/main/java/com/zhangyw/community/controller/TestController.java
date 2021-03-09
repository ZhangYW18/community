package com.zhangyw.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class TestController {

    @GetMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "hello world";
    }

    public void demoController(HttpServletRequest request, HttpServletResponse response) {

    }
}
