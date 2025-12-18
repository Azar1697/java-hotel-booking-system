package com.example.booking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Запускать ТОЛЬКО когда поднят Docker Compose") 
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullSystemTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper(); // <--- Добавили маппер
    private final String BASE_URL = "http://localhost:8090/api";

    private static String adminToken;
    private static String userToken;
    private static Long createdBookingId;

    @Test
    @Order(1)
    void loginAdmin_ShouldReturnToken() {
        System.out.println("--- 1. Логин Admin ---");
        adminToken = getToken("admin", "password");
        assertNotNull(adminToken, "Токен админа не должен быть null");
        System.out.println("✅ Админ залогинен: " + adminToken.substring(0, 10) + "...");
    }

    @Test
    @Order(2)
    void loginUser_ShouldReturnToken() {
        System.out.println("--- 2. Логин User ---");
        userToken = getToken("client", "12345");
        assertNotNull(userToken, "Токен юзера не должен быть null");
        System.out.println("✅ Юзер залогинен");
    }

    @Test
    @Order(3)
    void createAutoBooking_ShouldSuccess() throws Exception {
        System.out.println("--- 3. Создание брони (AutoSelect) ---");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "userId", 2,
            "hotelId", 1,
            "autoSelect", true,
            "startDate", "2045-01-01", // Далекое будущее, чтобы точно свободно
            "endDate", "2045-01-05"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // ИЗМЕНЕНИЕ: Получаем String, а не JsonNode
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/bookings", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Парсим строку руками
        JsonNode root = mapper.readTree(response.getBody());
        createdBookingId = root.get("id").asLong();
        
        System.out.println("✅ Бронь создана! ID: " + createdBookingId);
    }

    @Test
    @Order(4)
    void tryDoubleBooking_ShouldFail() {
        System.out.println("--- 4. Попытка двойной брони (Negative) ---");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "userId", 1,
            "hotelId", 1,
            "roomId", 2, 
            "startDate", "2045-01-01",
            "endDate", "2045-01-05"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            // ИЗМЕНЕНИЕ: String.class
            restTemplate.postForEntity(BASE_URL + "/bookings", request, String.class);
            fail("Должна была вылететь ошибка о занятости!");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("✅ Система поймала коллизию! Код: " + e.getStatusCode());
            assertTrue(e.getStatusCode().is4xxClientError() || e.getStatusCode().is5xxServerError());
        }
    }

    @Test
    @Order(5)
    void cancelBooking_ShouldSuccess() {
        System.out.println("--- 5. Отмена брони ---");
        // Проверка на случай, если предыдущий тест упал
        if (createdBookingId == null) {
            System.out.println("⚠️ Пропуск теста отмены (нет ID брони)");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = BASE_URL + "/bookings/" + createdBookingId + "?userId=2";

        ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.DELETE, request, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("✅ Бронь отменена");
    }

    // Вспомогательный метод получения токена
    private String getToken(String username, String password) {
        Map<String, String> body = Map.of("username", username, "password", password);
        try {
            // Запрашиваем ответ как строку
            ResponseEntity<String> response = restTemplate.postForEntity(
                    BASE_URL + "/auth/token", body, String.class);
            
            String responseBody = response.getBody();
            
            // ПРОВЕРКА: Если это JSON {"token": "..."}
            if (responseBody != null && responseBody.trim().startsWith("{")) {
                JsonNode root = mapper.readTree(responseBody);
                if (root.has("token")) {
                    return root.get("token").asText();
                }
            }
            
            // ИНАЧЕ: Это просто токен (Plain Text)
            return responseBody;
            
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить токен: " + e.getMessage());
        }
    }
}