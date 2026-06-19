package com.farmily.user.dto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(){

        return ResponseEntity.ok("Hello World");
    }

}
