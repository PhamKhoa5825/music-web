# --- Giai đoạn 1: Build code ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build ra file jar, bỏ qua test để tiết kiệm thời gian
RUN mvn clean package -DskipTests

# --- Giai đoạn 2: Chạy ứng dụng ---
FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
# Copy file jar sang (đã đổi từ war sang jar)
COPY --from=build /app/target/*.jar app.jar

# Mở port 8080
EXPOSE 8080

# CẤU HÌNH QUAN TRỌNG:
# 1. -Xmx350m: Giới hạn RAM để tránh out of memory
# 2. -Djava.security.egd=file:/dev/./urandom: Khởi động nhanh trên Linux
# 3. -Dserver.port=${PORT:-8080}: Đọc PORT từ environment, mặc định 8080
# 4. -Dserver.address=0.0.0.0: Cho phép truy cập từ bên ngoài
ENTRYPOINT ["sh", "-c", "java -Xmx350m -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar"]