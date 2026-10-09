package com.healsphere.dto;

import java.time.Instant;

/** JSON returned to the browser for one session. */
public record SessionResponse(Long id, String environment, String environmentName, int selectedSeconds,
                              int elapsedSeconds, boolean completed, Instant startedAt, Instant completedAt,
                              String mood) {}
