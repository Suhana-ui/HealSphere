package com.healsphere.dto;

import java.time.Instant;
import java.util.Map;

/** Standard error format for every failed API call. */
public record ApiError(int status, String error, String message, Instant timestamp, Map<String, String> fieldErrors) {}
