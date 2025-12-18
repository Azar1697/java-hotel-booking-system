package com.example.booking.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor // Создает конструктор со всеми полями (int, String, Date)
@NoArgsConstructor
public class AppError {
    private int statusCode;
    private String message;
    private Date timestamp;
}