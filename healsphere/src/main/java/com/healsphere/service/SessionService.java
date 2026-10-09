package com.healsphere.service;

import com.healsphere.dto.ProgressResponse;
import com.healsphere.dto.SessionRequest;
import com.healsphere.dto.SessionResponse;
import com.healsphere.exception.InvalidSessionException;
import com.healsphere.exception.ResourceNotFoundException;
import com.healsphere.model.TherapySession;
import com.healsphere.repository.TherapySessionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Business rules for saving sessions and calculating progress. */
@Service
public class SessionService {

    public static final Set<Integer> ALLOWED_DURATIONS = Set.of(60, 180, 300);
    public static final Set<String> ALLOWED_MOODS = Set.of("calm", "relaxed", "neutral", "tired", "stressed");

    private static final int TOLERANCE_SECONDS = 3;

    private final TherapySessionRepository repository;
    private final EnvironmentService environmentService;

    public SessionService(TherapySessionRepository repository, EnvironmentService environmentService) {
        this.repository = repository;
        this.environmentService = environmentService;
    }

    /** Result of a save: the record, and whether it was newly created. */
    public record SaveResult(SessionResponse session, boolean created) {}

    public SaveResult save(SessionRequest request) {
        // Same token already saved? Return the old record instead of creating a duplicate.
        var existing = repository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return new SaveResult(toResponse(existing.get()), false);
        }

        TherapySession entity = validateAndBuild(request);
        try {
            return new SaveResult(toResponse(repository.saveAndFlush(entity)), true);
        } catch (DataIntegrityViolationException race) {
            // Two identical requests arrived at once; the database unique constraint stopped the second.
            return repository.findByIdempotencyKey(request.idempotencyKey())
                    .map(found -> new SaveResult(toResponse(found), false))
                    .orElseThrow(() -> race);
        }
    }

    private TherapySession validateAndBuild(SessionRequest r) {
        var env = environmentService.find(r.environment())
                .orElseThrow(() -> new InvalidSessionException("Unknown environment. Choose forest, beach, mountain or room."));

        if (!ALLOWED_DURATIONS.contains(r.selectedSeconds())) {
            throw new InvalidSessionException("Duration must be 60, 180 or 300 seconds.");
        }

        String mood = null;
        if (r.mood() != null && !r.mood().isBlank()) {
            mood = r.mood().trim().toLowerCase(Locale.ROOT);
            if (!ALLOWED_MOODS.contains(mood)) {
                throw new InvalidSessionException("Mood must be one of: " + String.join(", ", ALLOWED_MOODS) + ".");
            }
        }

        Instant now = Instant.now();
        if (r.startedAt().isAfter(now.plusSeconds(TOLERANCE_SECONDS))) {
            throw new InvalidSessionException("Start time cannot be in the future.");
        }
        if (r.startedAt().isBefore(now.minus(Duration.ofHours(24)))) {
            throw new InvalidSessionException("Start time is too old.");
        }

        long wallClockSeconds = Duration.between(r.startedAt(), now).getSeconds();
        if (r.elapsedSeconds() > wallClockSeconds + TOLERANCE_SECONDS) {
            throw new InvalidSessionException("Elapsed time is longer than the real time since the session started.");
        }

        if (r.completed()) {
            // A completed session must have run for (almost) the full selected time.
            if (Math.abs(r.elapsedSeconds() - r.selectedSeconds()) > TOLERANCE_SECONDS) {
                throw new InvalidSessionException("A completed session must match the selected duration.");
            }
        } else if (r.elapsedSeconds() >= r.selectedSeconds()) {
            throw new InvalidSessionException("An incomplete session must be shorter than the selected duration.");
        }

        TherapySession s = new TherapySession();
        s.setEnvironment(env.key());
        s.setSelectedSeconds(r.selectedSeconds());
        s.setElapsedSeconds(r.elapsedSeconds());
        s.setCompleted(r.completed());
        s.setStartedAt(r.startedAt());
        s.setCompletedAt(r.completed() ? now : null); // server decides the completion time
        s.setMood(mood);
        s.setIdempotencyKey(r.idempotencyKey());
        return s;
    }

    public List<SessionResponse> getHistory(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return repository.findAllByOrderByStartedAtDesc(PageRequest.of(0, safeLimit))
                .stream().map(this::toResponse).toList();
    }

    public SessionResponse getById(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Session " + id + " was not found."));
    }

    /** Only sessions marked completed count toward the totals. */
    public ProgressResponse getProgress() {
        return new ProgressResponse(
                repository.countByCompleted(true),
                repository.countByCompleted(false),
                repository.sumCompletedSeconds());
    }

    private SessionResponse toResponse(TherapySession s) {
        String name = environmentService.find(s.getEnvironment()).map(e -> e.name()).orElse(s.getEnvironment());
        return new SessionResponse(s.getId(), s.getEnvironment(), name, s.getSelectedSeconds(),
                s.getElapsedSeconds(), s.isCompleted(), s.getStartedAt(), s.getCompletedAt(), s.getMood());
    }
}
