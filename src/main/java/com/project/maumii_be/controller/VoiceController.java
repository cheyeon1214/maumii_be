// src/main/java/.../controller/VoiceController.java
package com.project.maumii_be.controller;

import com.project.maumii_be.util.EncryptionUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@RestController
@RequestMapping("/api/voices")
@Tag(name = "Voice API", description = "녹음된 음성 파일 관련 API")
@CrossOrigin(
        origins = {
                "http://localhost:5050","http://127.0.0.1:5050",
                "http://192.168.210.13:5173","http://localhost:5173",
                "https://192.168.210.13:5173"
        },
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS},
        allowCredentials = "true",
        exposedHeaders = {"Accept-Ranges","Content-Range","Content-Length","Content-Type"}
)
public class VoiceController {
    // Spring Bean 으로 주입
    private final SecretKey secretKey;

    public VoiceController(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    // 컨테이너 실행 시 넣었던 -Dapp.upload.dir=/data/maumii/uploads/voices 와 매칭
    private final Path baseDir = Paths.get(System.getProperty("app.upload.dir", "/uploads/voices"));

    /** 파일명에 . 이 들어가도 매칭되도록 :.+ 추가 */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<?> getVoice(
            @PathVariable String fileName,
            @RequestHeader HttpHeaders headers) throws Exception {

        // 암호화 전 파일 경로
//        Path path = baseDir.resolve(fileName).normalize();
//        FileSystemResource file = new FileSystemResource(path.toFile());
//        if (!file.exists() || !file.isReadable()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
        // 암호화한 파일 경로
        Path encPath = baseDir.resolve(fileName).normalize();
        if (!Files.exists(encPath) || !Files.isReadable(encPath)) {
            return ResponseEntity.notFound().build();
        }

        // 임시 복호화 파일 생성
        Path tempWav = Files.createTempFile("dec_", ".wav");
        EncryptionUtil.decryptFile(encPath.toFile(), tempWav.toFile(), secretKey);

        // long fileLen = file.contentLength();
        // MediaType mt = guessMediaType(fileName);
        long fileLen = Files.size(tempWav);
        MediaType mt = MediaType.valueOf("audio/wav");

        // Range 없으면 전체 파일 스트리밍 (200 OK)
        if (headers.getRange().isEmpty()) {
            FileSystemResource resource = new FileSystemResource(tempWav.toFile());
            return ResponseEntity.ok()
                    .contentType(mt)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentLength(fileLen)
                    .body(resource); // .body(file);
        }

        // Range 1개만 처리 (브라우저 기본) -> 부분 범위 응답 (206 PARTIAL_CONTENT)
        HttpRange range = headers.getRange().get(0);
        long start = range.getRangeStart(fileLen);
        long end   = range.getRangeEnd(fileLen);
        if (end < start) end = fileLen - 1;
        long len = end - start + 1;

        byte[] buf = new byte[(int)Math.min(len, 1024L * 1024L)]; // 최대 1MB 청크

        try (InputStream is = Files.newInputStream(tempWav)) { // file.getInputStream()
            is.skip(start);
            int read = is.read(buf, 0, (int)Math.min(buf.length, len));
            if (read < 0) read = 0;
            ByteArrayResource chunk = new ByteArrayResource(buf);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(mt)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + (start + read - 1) + "/" + fileLen)
                    .contentLength(read)
                    .body(chunk);
        } finally {
            Files.deleteIfExists(tempWav); // 임시 파일 삭제
        }
    }

    private MediaType guessMediaType(String name) {
        String f = name.toLowerCase();
        if (f.endsWith(".mp3")) return MediaType.valueOf("audio/mpeg");
        if (f.endsWith(".wav")) return MediaType.valueOf("audio/wav");
        if (f.endsWith(".ogg") || f.endsWith(".oga")) return MediaType.valueOf("audio/ogg");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}