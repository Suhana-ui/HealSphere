package com.healsphere.model;

import jakarta.persistence.*;
import java.time.Instant;

/** One relaxation session saved in the database (table: therapy_session). */
@Entity
@Table(name = "therapy_session",
        uniqueConstraints = @UniqueConstraint(name = "uk_session_idem", columnNames = "idempotency_key"))
public class TherapySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String environment;

    @Column(name = "selected_seconds", nullable = false)
    private int selectedSeconds;

    @Column(name = "elapsed_seconds", nullable = false)
    private int elapsedSeconds;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(length = 20)
    private String mood;

    /** Random token made by the browser; stops the same session being saved twice. */
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 64)
    private String idempotencyKey;

    public Long getId() { return id; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public int getSelectedSeconds() { return selectedSeconds; }
    public void setSelectedSeconds(int selectedSeconds) { this.selectedSeconds = selectedSeconds; }
    public int getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(int elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
