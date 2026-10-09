package com.healsphere.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

/** JSON sent by the browser when saving a session. */
public record SessionRequest(
        @NotBlank(message = "environment is required") String environment,
        @NotNull(message = "selectedSeconds is required") Integer selectedSeconds,
        @NotNull(message = "elapsedSeconds is required") @Min(value = 0, message = "elapsedSeconds cannot be negative") Integer elapsedSeconds,
        @NotNull(message = "completed is required") Boolean completed,
        @NotNull(message = "startedAt is required") Instant startedAt,
        @Size(max = 20, message = "mood is too long") String mood,
        @NotBlank(message = "idempotencyKey is required")
        @Size(max = 64, message = "idempotencyKey is too long")
        @Pattern(regexp = "[A-Za-z0-9-]+", message = "idempotencyKey has invalid characters") String idempotencyKey) {}
