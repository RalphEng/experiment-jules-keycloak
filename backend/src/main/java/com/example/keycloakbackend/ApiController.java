package com.example.keycloakbackend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/public/data")
    public String getPublicData() {
        return "This is public data";
    }

    @GetMapping("/secure/data")
    public String getSecureData() {
        return "This is secured data";
    }
}