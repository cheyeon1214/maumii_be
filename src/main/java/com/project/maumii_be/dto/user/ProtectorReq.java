package com.project.maumii_be.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.Protector;
import com.project.maumii_be.domain.User;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProtectorReq {
    @JsonProperty("pEmail")
    String pEmail;
    @JsonProperty("uId")
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
