# Используем легкую версию Java 17
FROM amazoncorretto:17

# Рабочая папка внутри контейнера
WORKDIR /app

# Копируем собранный JAR файл внутрь контейнера
# (Maven при сборке кладет их в папку target/)
COPY target/*.jar app.jar

# Команда для запуска
ENTRYPOINT ["java", "-jar", "app.jar"]