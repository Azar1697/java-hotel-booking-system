package com.example.booking.controller;

import com.example.booking.entity.Booking;
import com.example.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingService.createBooking(booking);
    }

    // ИСПРАВЛЕНО: Полный метод с @RequestParam
    @DeleteMapping("/{id}")
    public void cancelBooking(@PathVariable Long id, @RequestParam Long userId) {
        bookingService.cancelBooking(id, userId);
    }
}