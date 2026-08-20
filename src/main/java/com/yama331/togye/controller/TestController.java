package com.yama331.togye.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class TestController {
    
    @GetMapping("/hi")
    public String hi() {
        return "hi";
    }
}
