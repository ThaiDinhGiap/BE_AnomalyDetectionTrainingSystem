# =============================================
# STAGE 1 — BUILD
# Dùng image có sẵn Maven + JDK 17 để build
# =============================================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy pom.xml trước — để Docker cache layer này
# Nếu code thay đổi nhưng pom.xml không đổi,
# Docker sẽ bỏ qua bước download dependency (tiết kiệm thời gian)
COPY pom.xml .
RUN apt-get update && apt-get install -y maven \
    && mvn dependency:go-offline -B

# Copy toàn bộ source code rồi mới build
COPY src ./src
RUN mvn clean package -DskipTests -B

# =============================================
# STAGE 2 — RUN
# Chỉ dùng JRE (nhẹ hơn JDK), không cần Maven nữa
# Image cuối cùng sẽ nhỏ hơn nhiều
# =============================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Tạo thư mục logs (application.yaml có cấu hình logging.file.path: ./logs)
RUN mkdir -p logs

# Copy file JAR từ stage builder sang
COPY --from=builder /app/target/anomaly-training-backend-0.0.1-SNAPSHOT.jar app.jar

# Cổng Spring Boot lắng nghe (mặc định 8080)
EXPOSE 8080

# Lệnh chạy app
# Các biến môi trường (DB_URL, JWT_SECRET...) sẽ được truyền vào từ docker-compose.yml
ENTRYPOINT ["java", "-jar", "app.jar"]