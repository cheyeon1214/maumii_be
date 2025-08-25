package com.project.maumii_be.domain;

import com.project.maumii_be.domain.enums.Emotion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Bubble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bId;

    private boolean bTalker;

    private LocalTime bLength;

    @Column(columnDefinition = "varchar(50) default 'calm'")
    @Enumerated(EnumType.STRING)
    private Emotion bEmotion = Emotion.calm;

    @Lob
    private String bText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "r_id")
    private Record record;
}
