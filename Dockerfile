# --- Giai đoạn 1: Build code ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build ra file war, bỏ qua test để tiết kiệm thời gian
RUN mvn clean package -DskipTests

# --- Giai đoạn 2: Chạy ứng dụng ---
FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
# Copy file war sang và đổi tên
COPY --from=build /app/target/*.war app.war

# Mở port 8080
EXPOSE 8080

# --- CẤU HÌNH QUAN TRỌNG NHẤT ---
# 1. -Djava.security.egd=file:/dev/./urandom  => Giúp khởi động cực nhanh trên Linux (Fix lỗi Timeout)
# 2. -Dserver.port=8080                       => Ép chạy port 8080
# 3. -Dserver.address=0.0.0.0                 => Mở kết nối ra ngoài để Render thấy
# ... (Các phần trên giữ nguyên)

# THÊM THAM SỐ -Xmx350m VÀO ĐẦU
ENTRYPOINT ["java", "-Xmx350m", "-Djava.security.egd=file:/dev/./urandom", "-Dserver.port=8080", "-Dserver.address=0.0.0.0", "-jar", "app.war"]