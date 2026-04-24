package com.xettuyen2026.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xettuyen2026.dto.ThiSinhRequest;
import com.xettuyen2026.service.ThiSinhService;

@RestController
@RequestMapping("/thisinh")
public class ThiSinhController {
    private final ThiSinhService tsService;

    public ThiSinhController() {
        this.tsService = new ThiSinhService();
    }
    
    @GetMapping("/")
    public String Testing() {
        return "Hello world!";
    }

    @PostMapping("/auth")
    public ResponseEntity<String> postMethodName(@RequestBody ThiSinhRequest request) {
        boolean valid = tsService.authenticate(request.getCccd(), request.getPassword());
        
        return valid ? ResponseEntity.ok("OK") : ResponseEntity.status(401).body("INVALID");
    }
    
    
}
