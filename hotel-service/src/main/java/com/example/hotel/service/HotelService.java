package com.example.hotel.service;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <--- Импорт
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j // <--- Включаем логи
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    // Создать отель
    @Transactional
    public Hotel createHotel(Hotel hotel) {
        log.info("Создание нового отеля: {}", hotel.getName());
        return hotelRepository.save(hotel);
    }

    // Создать комнату
    @Transactional
    public Room createRoom(Long hotelId, Room room) {
        log.info("Добавление комнаты {} в отель ID: {}", room.getNumber(), hotelId);
        
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> {
                    log.error("Отель ID {} не найден!", hotelId);
                    return new RuntimeException("Hotel not found");
                });
        
        room.setHotel(hotel);
        return roomRepository.save(room);
    }

    // Это часть САГИ (вызывается из Booking Service)
    @Transactional
    public boolean tryBookRoom(Long roomId) {
        log.info(">>> SAGA: Проверка доступности комнаты ID: {}", roomId);
        
        Room room = roomRepository.findById(roomId).orElse(null);
        
        if (room == null || !room.isAvailable()) {
            log.warn(">>> SAGA FAIL: Комната {} не найдена или недоступна", roomId);
            return false; 
        }
        
        // Логируем изменение статистики (Критерий 4)
        int oldVal = room.getTimesBooked();
        room.setTimesBooked(oldVal + 1);
        roomRepository.save(room);
        
        log.info(">>> SAGA SUCCESS: Комната {} забронирована. Популярность выросла: {} -> {}", 
                 roomId, oldVal, room.getTimesBooked());
        
        return true; 
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public List<Room> getRecommendedRooms() {
        List<Room> rooms = roomRepository.findByAvailableTrue();
        rooms.sort((r1, r2) -> Integer.compare(r1.getTimesBooked(), r2.getTimesBooked()));
        return rooms;
    }

    // АЛГОРИТМ (Критерий 1)
    public Room getBestRoom(Long hotelId) {
        log.info("ALGORITHM: Запуск поиска лучшей комнаты для отеля ID: {}", hotelId);
        
        // 1. Ищем свободные комнаты, отсортированные по популярности
        List<Room> rooms = roomRepository.findByHotelIdAndAvailableTrueOrderByTimesBookedAsc(hotelId);

        // 2. Если ничего нет — ошибка
        if (rooms.isEmpty()) {
            log.warn("ALGORITHM: Свободных мест нет!");
            throw new RuntimeException("Нет свободных номеров в этом отеле!");
        }

        Room bestRoom = rooms.get(0);
        log.info("ALGORITHM: Найдена комната ID: {} (Номер: {}). Бронировалась раз: {}", 
                 bestRoom.getId(), bestRoom.getNumber(), bestRoom.getTimesBooked());

        // 3. Возвращаем самую первую (самую "непопулярную")
        return bestRoom;
    }
}