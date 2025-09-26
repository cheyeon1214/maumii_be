package com.project.maumii_be.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.sound.sampled.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EncryptionPlayTest {
    // 로컬에서 암호화 복호화 재생 테스트 코드
    public static void main(String[] args) throws Exception {
        // 1️⃣ 테스트용 톤 WAV 생성
        Path original = Paths.get("test_tone.wav");
        generateToneWav(original.toFile(), 1, 440); // 1초, 440Hz

        // 2️⃣ 암호화 파일
        Path encrypted = Paths.get("test_tone.enc");

        // 3️⃣ 복호화 파일
        Path decrypted = Paths.get("test_tone_decrypted.wav");

        // 4️⃣ AES Key 생성
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey key = keyGen.generateKey();

        // 5️⃣ 암호화
        EncryptionUtil.encryptFile(original.toFile(), encrypted.toFile(), key);
        System.out.println("암호화 완료: " + encrypted.toAbsolutePath());

        // 6️⃣ 복호화
        EncryptionUtil.decryptFile(encrypted.toFile(), decrypted.toFile(), key);
        System.out.println("복호화 완료: " + decrypted.toAbsolutePath());

        // 7️⃣ 복호화 파일 재생
        playWav(decrypted.toFile());
        System.out.println("재생 완료");
    }

    /** 지정 초, 주파수 톤 WAV 생성 */
    private static void generateToneWav(File file, int seconds, double freq) throws Exception {
        float sampleRate = 16000f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        int frameSize = format.getFrameSize();
        int bufferSize = (int) (sampleRate * seconds * frameSize);
        byte[] data = new byte[bufferSize];

        double amplitude = 32760; // 16bit max
        for (int i = 0; i < bufferSize / 2; i++) {
            double angle = 2.0 * Math.PI * freq * i / sampleRate;
            short value = (short) (Math.sin(angle) * amplitude);
            data[i * 2] = (byte) (value & 0xff);
            data[i * 2 + 1] = (byte) ((value >> 8) & 0xff);
        }

        try (AudioInputStream ais = new AudioInputStream(
                new java.io.ByteArrayInputStream(data),
                format,
                data.length / frameSize
        )) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        }
    }

    /** WAV 파일 재생 */
    private static void playWav(File file) throws Exception {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            Thread.sleep(1000); // 1초 동안 재생
            clip.close();
        }
    }
}
