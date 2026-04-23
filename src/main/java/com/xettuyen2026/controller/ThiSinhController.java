package com.xettuyen2026.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ThiSinhController {
    
    @GetMapping("/")
    public String Testing() {
        return "Hello world!";
    }
    
}
