package com.project.maumii_be.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Builder
public class RecordList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rlId;

    @Column(length = 100)
    private String rlName;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_id")
    private User user;

    @Override
    public String toString() {
        return "RecordList{" +
                "rlId=" + rlId +
                ", rlName='" + rlName + '\'' +
                ", updateDate=" + updateDate +
                '}';
    }
}
