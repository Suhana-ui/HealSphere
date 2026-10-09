package com.healsphere.dto;

/** Statistics calculated from the database. */
public record ProgressResponse(long completedSessions, long incompleteSessions, long totalCompletedSeconds) {}
