# Giai đoạn 1: Build code (Dùng Maven và Java 17)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build ra file war, bỏ qua test để chạy cho nhanh
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng (Dùng bản Java nhẹ)
FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
# Copy file war vừa build ở trên sang đây, đổi tên thành app.war
COPY --from=build /app/target/*.war app.war
# ... (các phần trên giữ nguyên)

EXPOSE 8080

# SỬA DÒNG ENTRYPOINT THÀNH NHƯ SAU:
ENTRYPOINT ["java", "-Dserver.port=8080", "-Dserver.address=0.0.0.0", "-jar", "app.war"]