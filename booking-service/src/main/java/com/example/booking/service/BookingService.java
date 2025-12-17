package com.example.booking.service;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final WebClient.Builder webClientBuilder; // Для общения с отелями

    @Transactional
    public Booking createBooking(Booking booking) {
        // 1. Сразу ставим статус PENDING (в ожидании)
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        
        // Сохраняем в локальную базу (Локальная транзакция)
        Booking savedBooking = bookingRepository.save(booking);

        // 2. Делаем запрос в Hotel Service (Шаг согласования)
        // Используем имя сервиса "http://hotel-service/..."
        String url = "http://hotel-service/api/rooms/" + booking.getRoomId() + "/confirm-availability";

        Boolean isAvailable = Boolean.FALSE;
        
        try {
            isAvailable = webClientBuilder.build()
                    .post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block(); // block() делает запрос синхронным (ждем ответа)
        } catch (Exception e) {
            // Если сервис отелей недоступен или ошибка сети
            System.out.println("Ошибка при связи с Hotel Service: " + e.getMessage());
            isAvailable = false;
        }

        // 3. Обновляем статус в зависимости от ответа
        if (Boolean.TRUE.equals(isAvailable)) {
            savedBooking.setStatus(BookingStatus.CONFIRMED);
        } else {
            savedBooking.setStatus(BookingStatus.CANCELLED); // Компенсация (отмена)
        }

        return bookingRepository.save(savedBooking);
    }
}
