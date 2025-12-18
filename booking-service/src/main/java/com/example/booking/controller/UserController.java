package com.example.booking.controller;

import com.example.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @DeleteMapping
    public void deleteUser(@RequestParam Long id, 
                           @RequestHeader(value = "X-Authenticated-User", defaultValue = "user") String username) {
        if (!"admin".equals(username)) {
            throw new RuntimeException("403: Только Admin может удалять пользователей");
        }
        authService.deleteUser(id);
    }

    @PatchMapping
    public void updateUserRole(@RequestParam Long id, 
                               @RequestParam String role,
                               @RequestHeader(value = "X-Authenticated-User", defaultValue = "user") String username) {
        if (!"admin".equals(username)) {
            throw new RuntimeException("403: Только Admin может менять роли");
        }
        authService.updateUserRole(id, role);
    }
}