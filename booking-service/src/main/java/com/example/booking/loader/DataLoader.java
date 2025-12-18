package com.example.booking.loader;

import com.example.booking.entity.User;
import com.example.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final AuthService authService;

    @Override
    public void run(String... args) throws Exception {
        // Создаем Админа, только если его нет (проверку можно добавить в AuthService, но пока так)
        try {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("password");
            admin.setRole("ADMIN");
            authService.saveUser(admin);
            System.out.println(">>> [Booking Service] Пользователь 'admin' создан!");
        } catch (Exception e) {
            // Игнорируем, если уже есть
        }

        try {
            User client = new User();
            client.setUsername("client");
            client.setPassword("12345");
            client.setRole("USER");
            authService.saveUser(client);
            System.out.println(">>> [Booking Service] Пользователь 'client' создан!");
        } catch (Exception e) {
            // Игнорируем
        }
    }
}