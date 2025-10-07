package com.project.maumii_be.service.record;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GcsVoiceStorage {
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    @Value("${storage.bucket}") private String bucket;

    public String uploadWav(byte[] data, String contentType) {
        String object = "voices/" + java.util.UUID.randomUUID() + ".wav";
        BlobInfo info = BlobInfo.newBuilder(bucket, object)
                .setContentType(contentType != null ? contentType : "audio/wav")
                .build();
        storage.create(info, data);
        return object; // gs object path
    }

    public URL signedUrl(String object) {
        BlobInfo info = BlobInfo.newBuilder(bucket, object).build();
        return storage.signUrl(
                info,
                15, TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.httpMethod(HttpMethod.GET)
        );
    }
}