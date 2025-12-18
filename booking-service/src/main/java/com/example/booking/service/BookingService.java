package com.example.booking.service;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.repository.BookingRepository;
import lombok.Data; // <--- Нужно для RoomDTO
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration; // <--- Нужно для timeout
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public Booking createBooking(Booking booking) {
        log.info(">>> START: Создание брони. User: {}, Room: {}, Auto: {}", 
                 booking.getUserId(), booking.getRoomId(), booking.isAutoSelect());

        // -------------------------------------------------
        // 1. ЛОГИКА AUTO-SELECT (Критерий 1 и 3)
        // -------------------------------------------------
        if (booking.isAutoSelect()) {
             Long hotelId = booking.getHotelId();
             if (hotelId == null) throw new RuntimeException("Для автопобора нужен hotelId!");

             Long bestRoomId = fetchBestRoomId(hotelId);
             booking.setRoomId(bestRoomId);
             log.info("Автоподбор выбрал комнату ID: {}", bestRoomId);
        }

        // -------------------------------------------------
        // 2. ВАЛИДАЦИЯ ДАТ (Критерий 3)
        // -------------------------------------------------
        if (booking.getEndDate().isBefore(booking.getStartDate())) {
            throw new RuntimeException("Дата выезда должна быть позже даты заезда!");
        }

        boolean isOccupied = bookingRepository.existsByRoomIdAndStatusAndEndDateAfterAndStartDateBefore(
        booking.getRoomId(), 
        BookingStatus.CONFIRMED, 
        booking.getStartDate(), 
        booking.getEndDate()
        );

        if (isOccupied) {
            throw new RuntimeException("Эта комната уже занята на выбранные даты!");
        }

        // -------------------------------------------------
        // 3. СОХРАНЯЕМ PENDING
        // -------------------------------------------------
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        
        Booking savedBooking = bookingRepository.save(booking);

        // -------------------------------------------------
        // 4. ЗАПРОС В HOTEL SERVICE (САГА + RESILIENCE)
        // -------------------------------------------------
        String url = "http://hotel-service/api/hotels/rooms/" + booking.getRoomId() + "/confirm-availability";
        Boolean isAvailable = Boolean.FALSE;
        
        try {
            // ТУТ ДОБАВИЛИ TIMEOUT и RETRY (Критерий 2 - устойчивость)
            isAvailable = webClientBuilder.build()
                    .post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(10)) // Ждем максимум 10 сек
                    .retry(2)                       // Пробуем еще 2 раза, если сбой
                    .block();
        } catch (Exception e) {
            log.error("!!! Ошибка при связи с Hotel Service: {}", e.getMessage());
            isAvailable = false;
        }

        // -------------------------------------------------
        // 5. ОБНОВЛЕНИЕ СТАТУСА (КОМПЕНСАЦИЯ)
        // -------------------------------------------------
        if (Boolean.TRUE.equals(isAvailable)) {
            savedBooking.setStatus(BookingStatus.CONFIRMED);
        } else {
            savedBooking.setStatus(BookingStatus.CANCELLED);
        }

        return bookingRepository.save(savedBooking);
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        // Проверка: это бронь этого пользователя?
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("403: Вы не можете отменить чужую бронь!");
        }

        // Вместо удаления мы ставим статус CANCELLED (Soft Delete), чтобы осталась история
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Бронирование {} отменено пользователем {}", bookingId, userId);
    }

    // Вспомогательный метод для получения лучшей комнаты
    private Long fetchBestRoomId(Long hotelId) {
        String url = "http://hotel-service/api/hotels/" + hotelId + "/best";
        try {
            RoomDTO room = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RoomDTO.class)
                    .timeout(Duration.ofSeconds(10)) // Тут тоже тайм-аут не помешает
                    .block();
            return room.getId();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось подобрать номер автоматически: " + e.getMessage());
        }
    }
    
    // DTO для парсинга ответа от HotelService
    @Data
    static class RoomDTO { private Long id; }
}