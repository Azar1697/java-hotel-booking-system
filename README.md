Отличная работа! 🎉 Ты прошел огонь, воду и Docker Compose.Вот профессиональный README.md, который не стыдно показать на защите или выложить на GitHub. Он описывает все возможности, которые мы реализовали (включая автоподбор, защиту и отказоустойчивость).Скопируй этот текст в файл README.md в корне твоего проекта.Markdown# 🏨 Hotel Booking System (Microservices)

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-green) ![Docker](https://img.shields.io/badge/Docker-Compose-blue) ![Status](https://img.shields.io/badge/Status-Completed-success)

Высоконагруженная система бронирования отелей, построенная на микросервисной архитектуре. Проект реализует паттерн Saga, интеллектуальный алгоритм распределения номеров, отказоустойчивость (Resilience) и централизованную безопасность.

---

## 🚀 Основные возможности

* **Микросервисная архитектура:** Разделение на независимые сервисы (Booking, Hotel, Gateway, Discovery).
* **Умный алгоритм (Smart Allocation):** Система автоматически подбирает "лучшую" комнату для равномерного распределения нагрузки (`times_booked`).
* **Распределенные транзакции (Saga):** Гарантия согласованности данных. Если отель не подтверждает бронь, происходит автоматическая компенсация (отмена) в сервисе бронирования.
* **Отказоустойчивость (Resilience):** Реализованы тайм-ауты (3 сек) и повторные попытки (Retry) при недоступности сервисов.
* **Безопасность (Security):** JWT-аутентификация на шлюзе (Gateway) + Role-Based Access Control (RBAC) внутри сервисов.
* **Plug & Play:** Система автоматически предзаполняет базу данных пользователями и отелями при старте.

---

## 🛠 Технологический стек

* **Core:** Java 17, Spring Boot 3
* **Infrastructure:** Spring Cloud Gateway, Netflix Eureka
* **Communication:** Spring WebFlux (WebClient), REST
* **Database:** H2 (In-Memory) с поддержкой индексов для Highload
* **Deploy:** Docker, Docker Compose

---

## 🏗 Архитектура

Система состоит из 4-х контейнеров:

| Сервис | Порт | Описание |
| :--- | :--- | :--- |
| **Discovery Server** | `8761` | Реестр сервисов (Eureka). Все сервисы регистрируются здесь. |
| **API Gateway** | `8090` | Единая точка входа. Маршрутизация, проверка JWT, проброс заголовков безопасности. |
| **Booking Service** | `8082` | Управление бронированиями, Сага, Resilience логика. |
| **Hotel Service** | `8081` | Управление отелями, номерами, статистика популярности. |

---

## ▶️ Запуск проекта

Для запуска требуется установленный **Docker Desktop**.

1.  **Сборка JAR-файлов:**
    ```bash
    mvn clean package -DskipTests
    ```

2.  **Запуск контейнеров:**
    ```bash
    docker-compose up --build
    ```

*После запуска подождите 30-60 секунд для регистрации сервисов в Eureka.*

---

## 🔑 Доступы (Предзаполненные данные)

База данных автоматически наполняется при старте. Вы можете использовать этих пользователей:

| Роль | Username | Password | Возможности |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `password` | Создание отелей, номеров, просмотр всего. |
| **USER** | `client` | `12345` | Создание бронирований, просмотр истории. |

---

## 📡 API Documentation

Все запросы отправляются на порт **8090** (Gateway).

### 1️⃣ Аутентификация

**Получить токен (Login):**
`POST /api/auth/token`
```json
{
  "username": "admin",
  "password": "password"
}
В ответ придет JWT-токен. Используйте его в заголовке: Authorization: Bearer <TOKEN>.2️⃣ Отели (Hotel Service)МетодURLРольОписаниеGET/api/hotelsPublicПолучить список всех отелейPOST/api/hotelsADMINСоздать новый отельPOST/api/hotels/{id}/roomsADMINДобавить комнату в отельGET/api/hotels/{id}/bestPublicАлгоритм: Найти лучшую свободную комнатуПример создания отеля (Admin):Bashcurl -X POST http://localhost:8090/api/hotels \
-H "Authorization: Bearer <TOKEN>" \
-H "Content-Type: application/json" \
-d '{ "name": "Plaza", "city": "NY", "address": "5th Avenue" }'
3️⃣ Бронирование (Booking Service)МетодURLРольОписаниеPOST/api/bookingsUserСоздать бронирование (Ручное или Авто)GET/api/bookingsUserИстория бронирований🔥 Фича: Автоподбор номера (Auto-Select)Если вы не знаете ID комнаты, передайте флаг "autoSelect": true. Система сама найдет наименее загруженный номер в отеле.Пример запроса (Smart Booking):POST /api/bookingsJSON{
  "userId": 1,
  "hotelId": 1,
  "autoSelect": true, 
  "startDate": "2026-01-01",
  "endDate": "2026-01-05"
}
Ответ (Успех):JSON{
  "id": 1,
  "roomId": 102,       // <-- Система сама выбрала комнату
  "status": "CONFIRMED",
  ...
}
🛡 Отказоустойчивость и ТестированиеПроверка Resilience (Устойчивость)Если Hotel Service упадет или будет отвечать дольше 3 секунд, Booking Service не зависнет.Он сделает 2 повторные попытки (Retry).Если отель недоступен, бронь получит статус CANCELLED (Graceful Degradation).ТестированиеВ проекте реализованы Integration Tests (JUnit 5 + MockMvc):✅ Позитивный сценарий создания брони.✅ Негативный сценарий (валидация дат).✅ Проверка защиты ролей (403 Forbidden).Запуск тестов:Bashmvn test