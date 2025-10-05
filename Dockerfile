# --- Build stage (JDK + gradle wrapper 사용) ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 1) gradle wrapper와 설정만 먼저 복사 (캐시 극대화)
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle* ./
COPY build.gradle* ./

# 2) 실행 권한 및 버전 확인
RUN chmod +x ./gradlew && ./gradlew --version

# 3) 소스 복사 후 빌드
COPY src src
# 문제가 나면 원인 보려고 --stacktrace --info 추가
RUN ./gradlew clean bootJar -x test --no-daemon --stacktrace --info

# --- Run stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# ffmpeg 필요하면 유지
RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 빌드 산출물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 업로드 경로
RUN mkdir -p /data/maumii/uploads/voices
ENV JAVA_TOOL_OPTIONS="-Dapp.upload.dir=/data/maumii/uploads/voices"

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=cloudrun"]