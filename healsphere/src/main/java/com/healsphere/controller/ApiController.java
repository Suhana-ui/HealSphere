package com.healsphere.controller;

import com.healsphere.dto.EnvironmentDto;
import com.healsphere.dto.ProgressResponse;
import com.healsphere.dto.SessionRequest;
import com.healsphere.dto.SessionResponse;
import com.healsphere.service.EnvironmentService;
import com.healsphere.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** JSON endpoints used by the JavaScript (fetch) on the pages. */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final SessionService sessionService;
    private final EnvironmentService environmentService;

    public ApiController(SessionService sessionService, EnvironmentService environmentService) {
        this.sessionService = sessionService;
        this.environmentService = environmentService;
    }

    @GetMapping("/environments")
    public List<EnvironmentDto> environments() {
        return environmentService.getAll();
    }

    @GetMapping("/sessions")
    public List<SessionResponse> sessions(@RequestParam(defaultValue = "20") int limit) {
        return sessionService.getHistory(limit);
    }

    @GetMapping("/sessions/{id}")
    public SessionResponse session(@PathVariable Long id) {
        return sessionService.getById(id);
    }

    /** 201 Created for a new record, 200 OK when the same idempotency token was already saved. */
    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> save(@Valid @RequestBody SessionRequest request) {
        SessionService.SaveResult result = sessionService.save(request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.session());
    }

    @GetMapping("/progress")
    public ProgressResponse progress() {
        return sessionService.getProgress();
    }
}
