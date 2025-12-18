package com.example.booking.service;

import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    // ИСПРАВЛЕНО: переименовали repository -> userRepository
    private final UserRepository userRepository; 
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String saveUser(User user) {
        log.info("Попытка регистрации пользователя: {}", user.getUsername());
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user); // ИСПРАВЛЕНО
        
        log.info("Пользователь {} успешно зарегистрирован в системе", user.getUsername());
        return "User added to system";
    }

    public String generateToken(String username) {
        log.info("Запрос на генерацию токена для: {}", username);
        return jwtUtil.generateToken(username);
    }
    
    public void validateUser(String username, String password) {
         // Упрощение для учебного проекта
    }
    
    // Удаление пользователя (Admin)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удален", id);
    }

    // Обновление роли пользователя (Admin)
    public void updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setRole(newRole);
        userRepository.save(user);
        log.info("Роль пользователя {} изменена на {}", id, newRole);
    }
}