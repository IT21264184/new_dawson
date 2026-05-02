package com.example.Dawson.Bungalow.controller;
import com.example.Dawson.Bungalow.dto.*;
import com.example.Dawson.Bungalow.model.User;
import com.example.Dawson.Bungalow.repository.UserRepository;
import com.example.Dawson.Bungalow.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
