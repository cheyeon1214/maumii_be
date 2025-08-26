package com.project.maumii_be.dto;

import com.project.maumii_be.domain.Protector;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProtectorRes {
    Long pId;
    String pEmail;
    String uId;

    public ProtectorRes toProtectorRes(Protector protector) {
        return ProtectorRes.builder()
                .pId(protector.getPId())
                .pEmail(protector.getPEmail())
                .uId(protector.getUser().getUId())
                .build();
    }
}
