# --- Build stage ---
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

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

# Cloud Run은 환경변수 PORT를 주입함(보통 8080). 노출은 관습상 8080으로.
EXPOSE 8080

# 프로파일 하드코딩 금지
#ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=prod"]

# 표준 진입점
ENTRYPOINT ["java","-jar","/app/app.jar"]