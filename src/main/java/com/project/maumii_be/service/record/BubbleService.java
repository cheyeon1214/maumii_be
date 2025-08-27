package com.project.maumii_be.service.record;

import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.dto.BubbleReq;
import com.project.maumii_be.dto.BubbleRes;
import com.project.maumii_be.exception.BubbleSearchNotException;
import com.project.maumii_be.repository.BubbleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BubbleService {
    private final BubbleRepository bubbleRepository;

    // 녹음 리스트 상세 조회
    @Transactional(readOnly = true)
    public List<BubbleRes> getRecord(Long rlId) throws BubbleSearchNotException {
        List<Bubble> list = bubbleRepository.getRecord(rlId);
        if(list.isEmpty())
            throw new BubbleSearchNotException("해당 녹음 리스트에 내용이 없습니다.", "Not Found Record Info");
        return list.stream()
                .map(b -> new BubbleRes().toBubbleRes(b)).collect(Collectors.toList());
    }

    // 버블 정보 조회
    @Transactional(readOnly = true)
    public BubbleRes findById(Long bId) throws BubbleSearchNotException {
        Bubble bubble = bubbleRepository.findById(bId)
                .orElseThrow(() -> new BubbleSearchNotException("해당 버블이 없습니다.", "Not Found Bubble"));
        return new BubbleRes().toBubbleRes(bubble);
    }

    // 버블 텍스트 및 감정 수정
    public BubbleRes updateBubble(Long bId, BubbleReq bubbleReq) throws BubbleSearchNotException {
        Bubble bubbleEntity = bubbleRepository.findById(bId)
                .orElseThrow(() -> new BubbleSearchNotException("해당 버블이 없습니다.", "Not Found Bubble"));
        bubbleEntity.setBText(bubbleReq.getBText());
        bubbleEntity.setBEmotion(bubbleReq.getBEmotion());
        return new BubbleRes().toBubbleRes(bubbleEntity);
    }
}