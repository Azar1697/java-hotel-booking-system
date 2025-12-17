package com.example.booking.entity;

public enum BookingStatus {
    PENDING,   // Создано, ждет подтверждения от отеля
    CONFIRMED, // Отель подтвердил
    CANCELLED  // Ошибка или отмена
}
