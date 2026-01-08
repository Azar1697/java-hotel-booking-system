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

    // Метод пересечений
    boolean existsByRoomIdAndStatusAndEndDateAfterAndStartDateBefore(
            Long roomId, 
            BookingStatus status, 
            LocalDate startDate, 
            LocalDate endDate
    );
}
