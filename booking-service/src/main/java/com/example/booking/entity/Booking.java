package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.*; // <--- ВАЖНО: Звездочка импортирует все аннотации

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_user", columnList = "userId"),
    @Index(name = "idx_booking_room", columnList = "roomId")
})
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder // <--- Важно для создания объектов
@Data    // <--- Добавляет toString, equals, hashCode
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long roomId;
    
    // Поле для логики AutoSelect
    private Long hotelId; 

    // Поле не сохраняем в БД, оно нужно только для приема JSON
    @Transient 
    private boolean autoSelect; 
    
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;
}