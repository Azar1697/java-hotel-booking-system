package com.example.hotel.controller;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping("/hotels")
    public Hotel createHotel(@RequestBody Hotel hotel) {
        return hotelService.createHotel(hotel);
    }

    @PostMapping("/hotels/{hotelId}/rooms")
    public Room createRoom(@PathVariable Long hotelId, @RequestBody Room room) {
        return hotelService.createRoom(hotelId, room);
    }

    @GetMapping("/hotels")
    public List<Hotel> getHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/rooms/recommend")
    public List<Room> getRecommendedRooms() {
        return hotelService.getRecommendedRooms();
    }

    // Тот самый новый метод для связи с Booking Service
    @PostMapping("/rooms/{roomId}/confirm-availability")
    public boolean confirmAvailability(@PathVariable Long roomId) {
        return hotelService.tryBookRoom(roomId);
    }
} 
