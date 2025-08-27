package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.domain.enums.Emotion;
import com.project.maumii_be.dto.CreateRecordReq;
import com.project.maumii_be.dto.RecordReq;
import com.project.maumii_be.dto.RecordRes;
import com.project.maumii_be.exception.DMLException;
import com.project.maumii_be.repository.BubbleRepository;
import com.project.maumii_be.repository.RecordListRepository;
import com.project.maumii_be.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecordService {
    private final BubbleRepository bubbleRepository;
    private final RecordRepository recordRepository;
    private final RecordListRepository recordListRepository;
    private final Path root = Paths.get("uploads/voices");

    // 녹음 저장
    public RecordRes saveRecord(RecordReq recordReq) {
        // 버블 저장

        // 레코드 저장 (요청 DTO -> Entity)
        Record record = recordRepository.save(recordReq.toRecord(recordReq));

        // 레코드 리스트 저장

        return new RecordRes().toRecordRes(record); // Entity -> 응답 DTO
    }

    // 녹음 삭제
    public String deleteRecord(Long rId) {
        Record recordEntity = recordRepository.findById(rId)
                .orElseThrow(() -> new DMLException("Record 아이디 오류로 삭제 실패", "Wrong Record Id"));
        // 버블 삭제
        bubbleRepository.deleteByRId(rId);
        // 녹음 삭제
        recordRepository.deleteById(rId);
        return "Record DELETE OK";
    }

    // 녹음(음성) 파일 조회
    @Transactional(readOnly = true)
    public RecordRes findRecordVoice(Long rId) {
        Record record = recordRepository.findById(rId)
                .orElseThrow(() -> new DMLException("Record 아이디 오류로 삭제 실패", "Wrong Record Id"));
        return new RecordRes().toRecordRes(record);
    }

    public Long createRecord(CreateRecordReq req,
                             MultiValueMap<String, MultipartFile> files) {

        try { Files.createDirectories(root); } catch (IOException ignore) {}

        Record rec = new Record();

        // (옵션) 어떤 RecordList에 넣을지
        if (req.recordListId() != null) {
            RecordList rl = recordListRepository.findById(req.recordListId())
                    .orElseThrow(() -> new IllegalArgumentException("recordList not found: " + req.recordListId()));
            rec.setRecordList(rl);
        }

        // (옵션) 세션 통짜 오디오 파일이 올라오면 r_voice로 보관
        if (req.rVoiceField() != null) {
            MultipartFile rv = files.getFirst(req.rVoiceField());
            if (rv != null && !rv.isEmpty()) {
                rec.setRVoice(saveVoice(rv));  // 경로 문자열
            }
        }

        long sumMs = 0L;

        if (req.bubbles() != null) {
            for (var dto : req.bubbles()) {
                Bubble b = new Bubble();
                b.setRecord(rec);                            // FK
                b.setBTalker(Boolean.TRUE.equals(dto.getBTalker())); // true=내 말, false=상대
                if (dto.getBText() != null) b.setBText(dto.getBText());
                if (dto.getBLength() != null) {
                    b.setBLength(msToLocalTime(dto.getBLength().toNanoOfDay()));
                    sumMs += Math.max(0L, dto.getBLength().toNanoOfDay());
                }
                // 감정 없으면 calm
                b.setBEmotion(parseEmotion(dto.getBEmotion().toString()));

                // ★ 연관관계 편의 메서드: cascade로 같이 저장됨
                rec.getBubbles().add(b);
            }
        }

        // 세션 총 길이 (요청에 없으면 서버에서 합산한 값으로 세팅)
        if (rec.getRLength() == null && sumMs > 0) {
            rec.setRLength(msToLocalTime(sumMs));
        }

        recordRepository.save(rec);
        return rec.getRId();
    }

    private String saveVoice(MultipartFile file) {
        String filename = UUID.randomUUID() + resolveExt(file);
        Path dst = root.resolve(filename);
        try (var in = file.getInputStream()) {
            Files.copy(in, dst, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("voice save failed", e);
        }
        return dst.toString();
    }

    private String resolveExt(MultipartFile f) {
        String ct = (f.getContentType() == null ? "" : f.getContentType().toLowerCase(Locale.ROOT));
        if (ct.contains("ogg")) return ".ogg";
        if (ct.contains("webm")) return ".webm";
        if (ct.contains("wav")) return ".wav";
        if (ct.contains("mpeg") || ct.contains("mp3")) return ".mp3";
        return ".bin";
    }

    private LocalTime msToLocalTime(Long ms) {
        long sec = Math.max(0, ms / 1000);
        int nano = (int) ((ms % 1000) * 1_000_000);
        return LocalTime.ofSecondOfDay(sec).withNano(nano);
    }

    private Emotion parseEmotion(String s) {
        if (s == null || s.isBlank()) return Emotion.calm;
        try { return Emotion.valueOf(s.trim().toLowerCase(Locale.ROOT)); }
        catch (Exception ignore) { return Emotion.calm; }
    }
}