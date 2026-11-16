package com.devtype.identity.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCommand(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 20) String username,
        @NotBlank @Size(min = 8, max = 128) String password,
        String locale,
        String timezone,
        String guestToken,
        String userAgent,
        String ip
) {
}
