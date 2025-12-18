package com.example.hotel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
// ИНДЕКС на поле available (мы постоянно ищем свободные!)
@Table(name = "rooms", indexes = {
    @Index(name = "idx_room_available", columnList = "available"),
    @Index(name = "idx_room_hotel", columnList = "hotel_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;
    private boolean available; // true/false
    private int timesBooked;   // Статистика

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    @JsonIgnore
    private Hotel hotel;
}