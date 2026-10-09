package com.healsphere;

import com.healsphere.repository.TherapySessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:healsphere-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
class HealsphereApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired TherapySessionRepository repository;

    @BeforeEach
    void clean() { repository.deleteAll(); }

    /** Builds request JSON. The session "started" 'ago' seconds ago. */
    private String json(String env, int selected, int elapsed, boolean completed, String key, String mood, int ago) {
        String moodPart = mood == null ? "null" : "\"" + mood + "\"";
        return """
            {"environment":"%s","selectedSeconds":%d,"elapsedSeconds":%d,"completed":%b,
             "startedAt":"%s","mood":%s,"idempotencyKey":"%s"}
            """.formatted(env, selected, elapsed, completed, Instant.now().minusSeconds(ago), moodPart, key);
    }

    private org.springframework.test.web.servlet.ResultActions postSession(String body) throws Exception {
        return mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test void contextLoads() {}

    @Test void pagesReturnOk() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("HealSphere")));
        mvc.perform(get("/session")).andExpect(status().isOk());
        mvc.perform(get("/progress")).andExpect(status().isOk());
    }

    @Test void validEnvironmentIsShown() throws Exception {
        mvc.perform(get("/session?env=beach")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-env=\"beach\"")));
    }

    @Test void invalidOrMissingEnvironmentFallsBackToForest() throws Exception {
        mvc.perform(get("/session?env=nonsense")).andExpect(content().string(org.hamcrest.Matchers.containsString("data-env=\"forest\"")));
        mvc.perform(get("/session")).andExpect(content().string(org.hamcrest.Matchers.containsString("data-env=\"forest\"")));
    }

    @Test void environmentsEndpointReturnsFour() throws Exception {
        mvc.perform(get("/api/environments")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
    }

    @Test void completedSessionIsSavedWith201() throws Exception {
        postSession(json("forest", 60, 60, true, "key-1", "calm", 65))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.environmentName").value("Peaceful Forest"))
                .andExpect(jsonPath("$.completedAt").exists());
        assertEquals(1, repository.count());
    }

    @Test void duplicateSubmissionDoesNotCreateSecondRecord() throws Exception {
        String body = json("beach", 60, 60, true, "same-key", null, 65);
        MvcResult first = postSession(body).andExpect(status().isCreated()).andReturn();
        MvcResult second = postSession(body).andExpect(status().isOk()).andReturn();
        assertEquals(1, repository.count());
        assertEquals(first.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"),
                second.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test void invalidEnvironmentIsRejected() throws Exception {
        postSession(json("space", 60, 60, true, "k2", null, 65)).andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }

    @Test void invalidDurationIsRejected() throws Exception {
        postSession(json("forest", 120, 120, true, "k3", null, 130)).andExpect(status().isBadRequest());
    }

    @Test void invalidMoodIsRejected() throws Exception {
        postSession(json("forest", 60, 60, true, "k4", "angry", 65)).andExpect(status().isBadRequest());
    }

    @Test void missingFieldsGiveStructuredError() throws Exception {
        postSession("{}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.environment").exists());
    }

    @Test void malformedJsonIsBadRequestWithoutStackTrace() throws Exception {
        postSession("not json").andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Exception"))));
    }

    @Test void fakeCompletionIsRejected() throws Exception {
        // Claims completed but only 10 seconds elapsed
        postSession(json("forest", 60, 10, true, "k5", null, 15)).andExpect(status().isBadRequest());
        // Claims 60 seconds elapsed but the session only started 5 seconds ago
        postSession(json("forest", 60, 60, true, "k6", null, 5)).andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }

    @Test void incompleteSessionIsStoredButNotCounted() throws Exception {
        postSession(json("room", 180, 40, false, "k7", null, 45)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.completed").value(false)).andExpect(jsonPath("$.completedAt").doesNotExist());
        mvc.perform(get("/api/progress")).andExpect(jsonPath("$.completedSessions").value(0))
                .andExpect(jsonPath("$.incompleteSessions").value(1))
                .andExpect(jsonPath("$.totalCompletedSeconds").value(0));
    }

    @Test void incompleteSessionMustBeShorterThanSelected() throws Exception {
        postSession(json("room", 60, 60, false, "k8", null, 65)).andExpect(status().isBadRequest());
    }

    @Test void progressAndHistoryUseRealRecords() throws Exception {
        postSession(json("forest", 60, 60, true, "p1", null, 65)).andExpect(status().isCreated());
        postSession(json("beach", 180, 180, true, "p2", "relaxed", 185)).andExpect(status().isCreated());
        mvc.perform(get("/api/progress")).andExpect(jsonPath("$.completedSessions").value(2))
                .andExpect(jsonPath("$.totalCompletedSeconds").value(240));
        mvc.perform(get("/api/sessions")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test void emptyDatabaseGivesZeroProgress() throws Exception {
        mvc.perform(get("/api/progress")).andExpect(jsonPath("$.completedSessions").value(0))
                .andExpect(jsonPath("$.totalCompletedSeconds").value(0));
        mvc.perform(get("/api/sessions")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test void getSessionByIdAndMissingId() throws Exception {
        postSession(json("mountain", 60, 60, true, "g1", null, 65)).andExpect(status().isCreated());
        Long id = repository.findAll().get(0).getId();
        mvc.perform(get("/api/sessions/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.environment").value("mountain"));
        mvc.perform(get("/api/sessions/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/sessions/abc")).andExpect(status().isBadRequest());
    }
}
