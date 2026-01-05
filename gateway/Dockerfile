# 빌드
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true
COPY src ./src
RUN gradle clean build -x test --no-daemon

# 컨테이너 생성
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 보안을 위한 사용자 생성
RUN addgroup --system spring && adduser --system --group spring
USER spring:spring

# JVM 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
