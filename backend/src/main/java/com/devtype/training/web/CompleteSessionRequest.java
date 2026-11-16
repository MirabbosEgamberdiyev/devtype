package com.devtype.training.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CompleteSessionRequest(
        @NotBlank(message = "completeToken bo'sh bo'lishi mumkin emas")
        String completeToken,

        @NotBlank(message = "keylog bo'sh bo'lishi mumkin emas")
        String keylog,

        @PositiveOrZero(message = "clientElapsedMs musbat yoki 0 bo'lishi shart")
        int clientElapsedMs,

        ClientMetricsDto clientMetrics
) {
    public record ClientMetricsDto(
            Double wpm,
            Double accuracy
    ) {}
}
