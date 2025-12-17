package com.example.booking.controller;

import com.example.booking.entity.User;
import com.example.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public String addNewUser(@RequestBody User user) {
        return service.saveUser(user);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody User user) {
        // В реальном проекте тут нужно проверять пароль!
        return service.generateToken(user.getUsername());
    }
}