# --- Build stage ---
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

# --- Run stage ---
# --- Run stage ---
FROM eclipse-temurin:17-jre-jammy

# ✅ ffmpeg 설치 (영구 포함)
RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# 업로드 디렉토리 미리 생성
RUN mkdir -p /data/maumii/uploads/voices
ENV JAVA_TOOL_OPTIONS="-Dapp.upload.dir=/data/maumii/uploads/voices"

EXPOSE 9000
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=prod"]