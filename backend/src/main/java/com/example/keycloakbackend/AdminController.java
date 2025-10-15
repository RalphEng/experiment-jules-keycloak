package com.example.keycloakbackend;

import com.example.keycloakbackend.dto.UserDTO;
import com.example.keycloakbackend.service.KeycloakAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('APPX-Admin')")
public class AdminController {

    private final KeycloakAdminService keycloakAdminService;

    public AdminController(KeycloakAdminService keycloakAdminService) {
        this.keycloakAdminService = keycloakAdminService;
    }

    @GetMapping("/data")
    public String getAdminData() {
        return "This is admin data";
    }

    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return keycloakAdminService.getUsers();
    }
}