package com.example.hotel.controller;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <--- ВАЖНО: Импорт
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j // <--- ВАЖНО: Аннотация, чтобы работал log.error
public class HotelController {

    private final HotelService hotelService;

    // Создать отель (ТОЛЬКО ДЛЯ АДМИНА)
    @PostMapping
    public Hotel createHotel(@RequestBody Hotel hotel, 
                             @RequestHeader(value = "X-Authenticated-User", defaultValue = "user") String username) {
        
        // Проверка безопасности
        if (!"admin".equals(username)) {
            log.error("Попытка создания отеля пользователем без прав: {}", username);
            throw new RuntimeException("403: Только администраторы могут создавать отели!");
        }

        return hotelService.createHotel(hotel);
    }

    // Создать комнату
    @PostMapping("/{hotelId}/rooms")
    public Room createRoom(@PathVariable Long hotelId, @RequestBody Room room) {
        return hotelService.createRoom(hotelId, room);
    }

    // Получить все отели
    @GetMapping
    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    // Получить лучшую комнату (Алгоритм)
    @GetMapping("/{hotelId}/best")
    public Room getBestRoom(@PathVariable Long hotelId) {
        return hotelService.getBestRoom(hotelId);
    }
    
    // Внутренний метод для проверки доступности (SAGA)
    @PostMapping("/rooms/{roomId}/confirm-availability")
    public boolean checkAvailability(@PathVariable Long roomId) {
        return hotelService.tryBookRoom(roomId);
    }
}