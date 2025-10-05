# --- Build stage ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# gradle wrapper 관련 파일만 먼저 복사 (캐시 활용)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 나머지 소스 복사
COPY src src

# gradlew 실행 권한 부여 후 빌드 (테스트 생략)
RUN chmod +x ./gradlew \
 && ./gradlew clean bootJar -x test --no-daemon

# --- Run stage ---
FROM eclipse-temurin:17-jre-jammy

# ffmpeg 포함
RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# 업로드 디렉토리 (런타임에 쓸 경로)
RUN mkdir -p /data/maumii/uploads/voices

# 업로드 경로를 시스템 프로퍼티로 주입
ENV JAVA_TOOL_OPTIONS="-Dapp.upload.dir=/data/maumii/uploads/voices"

# Cloud Run이 PORT 환경변수를 주입하므로 EXPOSE는 관습적으로 8080만 지정
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=cloudrun"]