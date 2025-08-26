package com.project.maumii_be.dto;

import com.project.maumii_be.domain.Protector;
import com.project.maumii_be.domain.User;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProtectorReq {
    String pEmail;
    String uId;

    public Protector toProtector(ProtectorReq protectorReq) {
        return Protector.builder()
                .pEmail(protectorReq.getPEmail())
                .user(User.builder()
                        .uId(protectorReq.getUId())
                        .build())
                .build();
    }
}
