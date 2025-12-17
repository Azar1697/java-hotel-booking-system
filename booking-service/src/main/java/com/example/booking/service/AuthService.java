package com.example.booking.service;

import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        return "User added to system";
    }

    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }
    
    // Простейшая проверка (в реальном проекте используем AuthenticationManager)
    public void validateUser(String username, String password) {
         // Упрощение для учебного проекта
    }
}