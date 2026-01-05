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
COPY --from=build /app/build/libs/*.jar app.jar

# spring 시스템 그 생성 & spring 시스템 사용자를 생성하고 그룹에 추가
RUN addgroup --system spring && adduser --system --group spring

# 모든 명령어를 spring 사용자 권한으로 실행
USER spring:spring

# JVM 옵션 최적화
ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
