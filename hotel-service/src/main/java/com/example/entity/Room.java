package com.example.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number; // Номер комнаты (строка, т.к. может быть "101A")

    // Операционная доступность (например, false, если ремонт)
    // Не путать с занятостью бронированием!
    private boolean available; 

    @Column(name = "times_booked")
    private int timesBooked; // Счетчик для алгоритма популярности

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    @JsonIgnore // Чтобы не было бесконечной рекурсии при сериализации в JSON
    private Hotel hotel;
}
