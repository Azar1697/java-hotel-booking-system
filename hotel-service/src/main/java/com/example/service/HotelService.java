package com.example.hotel.service;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    // Создать отель
    @Transactional
    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    // Создать комнату
    @Transactional
    public Room createRoom(Long hotelId, Room room) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        room.setHotel(hotel);
        return roomRepository.save(room);
    }

    @Transactional
    public boolean tryBookRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || !room.isAvailable()) {
            return false; // Комнаты нет или она на ремонте
        }
        
        // Увеличиваем счетчик популярности (как в задании)
        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);
        
        return true; // Успешно забронировали
    }

    // Получить все отели
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    // Рекомендации (свободные номера)
    public List<Room> getRecommendedRooms() {
        List<Room> rooms = roomRepository.findByAvailableTrue();
        rooms.sort((r1, r2) -> Integer.compare(r1.getTimesBooked(), r2.getTimesBooked()));
        return rooms;
    }
}
