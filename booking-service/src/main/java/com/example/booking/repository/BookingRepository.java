package com.example.booking.repository;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Для истории пользователя
    List<Booking> findByUserId(Long userId);

    // 🔥 ТОТ САМЫЙ МЕТОД ДЛЯ ПРОВЕРКИ ПЕРЕСЕЧЕНИЙ
    // Логика: Найти бронь, где (Комната та же) И (Статус CONFIRMED) 
    // И (КонецСуществующей > НачалоНовой) И (НачалоСуществующей < КонецНовой)
    boolean existsByRoomIdAndStatusAndEndDateAfterAndStartDateBefore(
            Long roomId, 
            BookingStatus status, 
            LocalDate startDate, 
            LocalDate endDate
    );
}