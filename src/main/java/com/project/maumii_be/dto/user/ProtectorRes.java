package com.project.maumii_be.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.Protector;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProtectorRes {
    @JsonProperty("pId")
    Long pId;

    @JsonProperty("pEmail")
    String pEmail;

    @JsonProperty("uId")
    String uId;

    public ProtectorRes toProtectorRes(Protector protector) {
        return ProtectorRes.builder()
                .pId(protector.getPId())
                .pEmail(protector.getPEmail())
                .uId(protector.getUser().getUId())
                .build();
    }

    public ProtectorRes(Protector protector) {
        this.pId = protector.getPId();
        this.pEmail = protector.getPEmail();
        this.uId = protector.getUser().getUId();
    }
}
