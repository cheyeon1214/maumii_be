package com.project.maumii_be.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
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

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bubble> bubbles = new ArrayList<>();

    @Override
    public String toString() {
        return "Record{" +
                "rId=" + rId +
                ", rCreatedAt=" + rCreatedAt +
                ", rLength=" + rLength +
                ", rVoice='" + rVoice + '\'' +
                '}';
    }

    public void addBubble(Bubble b) {
        if (bubbles == null) bubbles = new ArrayList<>();
        bubbles.add(b);
        b.setRecord(this);
    }
}
