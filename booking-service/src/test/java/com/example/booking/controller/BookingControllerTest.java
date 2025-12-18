package com.example.booking.controller;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.service.AuthService;
import com.example.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false) // Отключаем Security для тестов
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    // ТЕСТ 1: Позитивный (Всё хорошо)
    @Test
    void createBooking_ShouldReturnCreatedBooking() throws Exception {
        // Arrange
        Booking bookingRequest = new Booking();
        bookingRequest.setRoomId(102L);
        bookingRequest.setUserId(1L);
        bookingRequest.setStartDate(LocalDate.now());
        bookingRequest.setEndDate(LocalDate.now().plusDays(1));

        Booking savedBooking = new Booking();
        savedBooking.setId(555L);
        savedBooking.setRoomId(102L);
        savedBooking.setStatus(BookingStatus.CONFIRMED);
        savedBooking.setStartDate(LocalDate.now());
        savedBooking.setEndDate(LocalDate.now().plusDays(1));

        when(bookingService.createBooking(any(Booking.class))).thenReturn(savedBooking);

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(555))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // ТЕСТ 2: Негативный (Ошибка валидации дат) - НОВЫЙ!
    @Test
    void createBooking_WithInvalidDates_ShouldReturnBadRequest() throws Exception {
        // Arrange: Создаем бронь с ошибкой (выезд раньше заезда)
        Booking badBooking = new Booking();
        badBooking.setRoomId(102L);
        badBooking.setUserId(1L);
        badBooking.setStartDate(LocalDate.now());
        badBooking.setEndDate(LocalDate.now().minusDays(1)); // ОШИБКА: Дата в прошлом

        // Моделируем поведение сервиса: "Если тебе дадут любую бронь, кидай ошибку"
        // (В реальной жизни сервис сам бы выбросил её из-за нашего if, но в тесте мы управляем сервисом)
        when(bookingService.createBooking(any(Booking.class)))
                .thenThrow(new RuntimeException("Дата выезда должна быть позже даты заезда!"));

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badBooking)))
                .andExpect(status().isBadRequest()) // Ожидаем 400 Bad Request
                // Проверяем, что вернулся наш красивый JSON с ошибкой (благодаря GlobalExceptionHandler)
                .andExpect(jsonPath("$.message").value("Дата выезда должна быть позже даты заезда!"));
    }
}