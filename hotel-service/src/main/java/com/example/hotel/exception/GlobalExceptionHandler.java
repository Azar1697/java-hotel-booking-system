package com.example.hotel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date; // <--- Не забудь импорт

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AppError> handleRuntimeException(RuntimeException e) {
        // Если это ошибка доступа (403)
        if (e.getMessage().contains("403")) {
            return new ResponseEntity<>(
                // ДОБАВИЛИ new Date() третьим параметром
                new AppError(HttpStatus.FORBIDDEN.value(), "ACCESS DENIED: " + e.getMessage(), new Date()),
                HttpStatus.FORBIDDEN
            );
        }

        // Стандартная ошибка (400)
        return new ResponseEntity<>(
            // ДОБАВИЛИ new Date() третьим параметром
            new AppError(HttpStatus.BAD_REQUEST.value(), e.getMessage(), new Date()),
            HttpStatus.BAD_REQUEST
        );
    }
}