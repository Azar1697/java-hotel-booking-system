package com.example.hotel.loader;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HotelDataLoader implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        if (hotelRepository.count() > 0) return;

        Hotel hotel = new Hotel();
        hotel.setName("Grand Hotel Moscow");
        // hotel.setCity("Moscow"); // Убедись, что поле называется city, или удали эту строку если поля нет
        hotel.setAddress("Tverskaya st, 1");
        
        hotel = hotelRepository.save(hotel);

        Room room1 = new Room();
        room1.setNumber("101");
        room1.setAvailable(true);
        room1.setTimesBooked(10);
        room1.setHotel(hotel);

        Room room2 = new Room();
        room2.setNumber("102");
        room2.setAvailable(true);
        room2.setTimesBooked(0);
        room2.setHotel(hotel);

        Room room3 = new Room();
        room3.setNumber("103");
        room3.setAvailable(false);
        room3.setTimesBooked(5);
        room3.setHotel(hotel);

        roomRepository.saveAll(List.of(room1, room2, room3));
        System.out.println(">>> [Hotel Service] Данные загружены!");
    }
}