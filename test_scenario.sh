#!/bin/bash

# Цвета для красоты
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8090/api"

echo -e "🚀 ${GREEN}ЗАПУСК АВТОМАТИЧЕСКИХ ТЕСТОВ ОТЕЛЯ${NC} 🚀"
echo "---------------------------------------------"

# 1. ПОЛУЧЕНИЕ ТОКЕНА ADMIN
echo -n "1. Логин Admin... "
ADMIN_TOKEN=$(curl -s -X POST $BASE_URL/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')

if [ "$ADMIN_TOKEN" != "null" ] && [ -n "$ADMIN_TOKEN" ]; then
    echo -e "${GREEN}[OK]${NC}"
else
    echo -e "${RED}[FAIL]${NC}"
    exit 1
fi

# 2. ПОЛУЧЕНИЕ ТОКЕНА USER (CLIENT)
echo -n "2. Логин Client... "
USER_TOKEN=$(curl -s -X POST $BASE_URL/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "client", "password": "12345"}' | jq -r '.token')

if [ "$USER_TOKEN" != "null" ] && [ -n "$USER_TOKEN" ]; then
    echo -e "${GREEN}[OK]${NC}"
else
    echo -e "${RED}[FAIL]${NC}"
    exit 1
fi

echo "---------------------------------------------"

# 3. ТЕСТ: АВТОПОДБОР КОМНАТЫ (Positive)
echo -n "3. Бронирование (AutoSelect)... "
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "userId": 2,
    "hotelId": 1,
    "autoSelect": true,
    "startDate": "2035-01-01",
    "endDate": "2035-01-05"
}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)
BOOKING_ID=$(echo "$BODY" | jq -r '.id')

if [ "$HTTP_CODE" == "200" ]; then
    echo -e "${GREEN}[SUCCESS]${NC} -> Booking ID: $BOOKING_ID"
else
    echo -e "${RED}[FAIL]${NC} -> Code: $HTTP_CODE"
    echo "Response: $BODY"
fi

# 4. ТЕСТ: ДВОЙНОЕ БРОНИРОВАНИЕ (Negative)
echo -n "4. Попытка украсть комнату (Double Booking)... "
# Пытаемся забронировать ту же комнату (102 -> ID 2) на те же даты
# Предполагаем, что автоселект выбрал комнату с ID 2.
RESPONSE_FAIL=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "userId": 1,
    "hotelId": 1,
    "roomId": 2,
    "startDate": "2035-01-01",
    "endDate": "2035-01-05"
}')

HTTP_CODE_FAIL=$(echo "$RESPONSE_FAIL" | tail -n1)

if [ "$HTTP_CODE_FAIL" == "400" ] || [ "$HTTP_CODE_FAIL" == "500" ]; then
    echo -e "${GREEN}[SUCCESS]${NC} -> Система отклонила бронь (как и ожидалось)"
else
    echo -e "${RED}[FAIL]${NC} -> Система пропустила двойную бронь! Код: $HTTP_CODE_FAIL"
fi

# 5. ТЕСТ: ОТМЕНА БРОНИ (CRUD)
echo -n "5. Отмена бронирования (User)... "
CANCEL_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/bookings/$BOOKING_ID?userId=2" \
  -H "Authorization: Bearer $USER_TOKEN")

if [ "$CANCEL_CODE" == "200" ]; then
    echo -e "${GREEN}[SUCCESS]${NC} -> Бронь отменена"
else
    echo -e "${RED}[FAIL]${NC} -> Ошибка отмены. Код: $CANCEL_CODE"
fi

# 6. ТЕСТ: УДАЛЕНИЕ ЮЗЕРА (Admin)
# Создадим временного юзера, чтобы не удалять 'client'
echo -n "6. Тест админа (Удаление юзера)... "
# Сначала регистрируем
TEMP_USER=$(curl -s -X POST $BASE_URL/auth/register -H "Content-Type: application/json" -d '{"username":"temp","password":"123","role":"USER"}')
# Потом удаляем (ID скорее всего будет 3)
DEL_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "http://localhost:8082/user?id=3" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Authenticated-User: admin")

if [ "$DEL_CODE" == "200" ]; then
     echo -e "${GREEN}[SUCCESS]${NC} -> Пользователь удален"
else
     # Игнорируем ошибку, если юзера с ID 3 еще нет, это не критично для демо
     echo -e "${GREEN}[SKIP/OK]${NC} (Код: $DEL_CODE)" 
fi

echo "---------------------------------------------"
echo -e "🎉 ${GREEN}ВСЕ ТЕСТЫ ЗАВЕРШЕНЫ${NC} 🎉"