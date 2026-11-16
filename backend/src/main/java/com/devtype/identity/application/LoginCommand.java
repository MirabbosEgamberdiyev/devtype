package com.devtype.identity.application;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank String login,
        @NotBlank String password,
        String userAgent,
        String ip
) {
}
