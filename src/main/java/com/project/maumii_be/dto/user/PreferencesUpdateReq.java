package com.project.maumii_be.dto.user;

import com.project.maumii_be.domain.enums.Theme;
import lombok.Data;

public record PreferencesUpdateReq(
        com.project.maumii_be.domain.enums.Theme uTheme, // null이면 변경 안 함
        Boolean uExposure                                // null이면 변경 안 함
) {}
