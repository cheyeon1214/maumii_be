// src/main/java/com/project/maumii_be/service/record/RecordService.java
package com.project.maumii_be.service.record;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.dto.record.RecordRes;
import com.project.maumii_be.exception.DMLException;
import com.project.maumii_be.exception.RecordSearchNotException;
import com.project.maumii_be.repository.BubbleRepository;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import com.project.maumii_be.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecordService {

    private final BubbleRepository bubbleRepository;
    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;

    // GCS 설정
    @Value("${storage.bucket}")
    private String bucketName;
    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    // RecordSaveService와 동일: DB에는 "voices/xxxx.enc" 형태의 오브젝트 이름이 들어감
    private static final String GCS_PREFIX = "voices/";

    // 암호화 키 (Bean 주입)
    private final SecretKey secretKey;

    /** DB에 저장된 값이 유효한 GCS 오브젝트 이름인지 간단히 정규화 */
    private String normalizeObjectName(String stored) {
        if (stored == null || stored.isBlank()) return null;
        // 혹시 "gs://bucket/voices/..." 같은 게 들어와도 뒷부분만 추출
        int idx = stored.indexOf(GCS_PREFIX);
        return (idx >= 0) ? stored.substring(idx) : stored;
    }

    // ---------- 삭제: DB + GCS 객체 ----------
    public String deleteByRIdIn(Collection<Long> rIds) throws RecordSearchNotException {
        List<Record> list = recordRepository.findByrIdIn(rIds);

        // 1) GCS 객체 삭제 (없어도 무시)
        for (Record r : list) {
            String objectName = normalizeObjectName(r.getRVoice());
            if (objectName != null) {
                try {
                    storage.delete(BlobId.of(bucketName, objectName));
                } catch (Exception ignore) {
                    // 파일 미존재/권한 문제여도 비즈니스는 계속
                }
            }
        }

        // 2) 레코드 삭제 (버블은 cascade)
        recordRepository.deleteAll(list);
        return "Record DELETE OK";
    }

    // ---------- 조회: 그대로 매핑 (rVoice에는 voices/xxxx.enc 가 들어있음) ----------
    @Transactional(readOnly = true)
    public RecordRes findRecordVoice(Long rId) {
        Record record = recordRepository.findById(rId)
                .orElseThrow(() -> new DMLException("Record 아이디 오류로 조회 실패", "Wrong Record Id"));
        return new RecordRes().toRecordRes(record);
    }

    /**
     * 재생을 위해:
     * 1) GCS에서 enc 바이트를 임시파일로 다운로드
     * 2) 복호화하여 WAV 임시파일로 반환
     * 컨트롤러는 이 Path를 파일스트림으로 내려주면 됨.
     */
    @Transactional(readOnly = true)
    public Path getDecryptedVoicePath(Long rId) throws Exception {
        Record record = recordRepository.findById(rId)
                .orElseThrow(() -> new DMLException("Record 아이디 오류로 조회 실패", "Wrong Record Id"));

        String objectName = normalizeObjectName(record.getRVoice()); // 예: voices/xxx.enc
        if (objectName == null) throw new DMLException("음성 경로가 비어있음", "Voice path empty");

        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        if (blob == null || !blob.exists()) throw new DMLException("GCS 객체가 없음", "GCS object not found");

        // enc 임시 파일 저장
        Path encTmp = Files.createTempFile("voice_", ".enc");
        Files.write(encTmp, blob.getContent());

        // 복호화 임시 WAV
        Path wavTmp = Files.createTempFile("play_", ".wav");
        try {
            EncryptionUtil.decryptFile(encTmp.toFile(), wavTmp.toFile(), secretKey);
        } finally {
            // enc 임시파일은 즉시 정리
            try { Files.deleteIfExists(encTmp); } catch (Exception ignore) {}
        }
        return wavTmp;
    }
}