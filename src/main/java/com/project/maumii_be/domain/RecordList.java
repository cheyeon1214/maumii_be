package com.project.maumii_be.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_id")
    private User user;

    @Override
    public String toString() {
        return "RecordList{" +
                "rlName='" + rlName + '\'' +
                ", rlId=" + rlId +
                '}';
    }
}
