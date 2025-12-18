package com.example.hotel.repository;

import com.example.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // Тот самый сложный метод для алгоритма (уже есть)
    List<Room> findByHotelIdAndAvailableTrueOrderByTimesBookedAsc(Long hotelId);

    // ---> ДОБАВЬ ВОТ ЭТУ СТРОКУ <---
    // Она нужна, чтобы починить ошибку компиляции
    List<Room> findByAvailableTrue();
}