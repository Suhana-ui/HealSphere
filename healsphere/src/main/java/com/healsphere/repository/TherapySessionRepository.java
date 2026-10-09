package com.healsphere.repository;

import com.healsphere.model.TherapySession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/** Spring Data JPA writes the SQL for these methods automatically. */
public interface TherapySessionRepository extends JpaRepository<TherapySession, Long> {

    Optional<TherapySession> findByIdempotencyKey(String idempotencyKey);

    List<TherapySession> findAllByOrderByStartedAtDesc(Pageable pageable);

    long countByCompleted(boolean completed);

    @Query("select coalesce(sum(s.elapsedSeconds), 0) from TherapySession s where s.completed = true")
    long sumCompletedSeconds();
}
