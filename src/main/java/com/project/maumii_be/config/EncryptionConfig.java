package com.project.maumii_be.config;

import com.project.maumii_be.util.EncryptionUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class EncryptionConfig {
    @Bean
    public SecretKey secretKey() throws Exception {
        return EncryptionUtil.generateKey();
    }
}
