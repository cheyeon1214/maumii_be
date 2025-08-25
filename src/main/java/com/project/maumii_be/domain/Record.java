package com.project.maumii_be.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rId;

    @CreationTimestamp
    private LocalDateTime rCreatedAt;

    private LocalTime rLength;

    @Lob
    private String rVoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rl_id")
    private RecordList recordList;

    @Override
    public String toString() {
        return "Record{" +
                "rId=" + rId +
                ", rCreatedAt=" + rCreatedAt +
                ", rLength=" + rLength +
                ", rVoice='" + rVoice + '\'' +
                '}';
    }
}
