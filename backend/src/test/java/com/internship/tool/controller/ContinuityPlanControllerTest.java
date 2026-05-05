package com.internship.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.ContinuityPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests — spins up the full Spring context with H2 in-memory DB.
 * Flyway is disabled so no migrations run; JPA creates tables from entities.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:bcp_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ContinuityPlanControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    // ── GET /api/plans ─────────────────────────────────────────────────────

    @Test
    void getAll_returns200() throws Exception {
        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ── POST /api/plans ────────────────────────────────────────────────────

    @Test
    void createPlan_returns201() throws Exception {
        ContinuityPlan plan = buildPlan("Server Failover", "Active");
        mockMvc.perform(post("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(plan)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Server Failover"))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    void createPlan_missingTitle_returns400() throws Exception {
        ContinuityPlan bad = new ContinuityPlan();
        bad.setStatus("Pending");
        mockMvc.perform(post("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    // ── GET /api/plans/{id} ────────────────────────────────────────────────

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/plans/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getById_found_returns200() throws Exception {
        String json = createAndReturnBody("DR Plan", "Active");
        Long id = mapper.readTree(json).get("id").asLong();

        mockMvc.perform(get("/api/plans/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    // ── PUT /api/plans/{id} ────────────────────────────────────────────────

    @Test
    void updatePlan_changesStatus() throws Exception {
        String json = createAndReturnBody("Old Title", "Pending");
        Long id = mapper.readTree(json).get("id").asLong();

        ContinuityPlan updated = buildPlan("Old Title", "Active");
        mockMvc.perform(put("/api/plans/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Active"));
    }

    // ── DELETE /api/plans/{id} ─────────────────────────────────────────────

    @Test
    void deletePlan_returns204_thenGetReturns404() throws Exception {
        String json = createAndReturnBody("Plan To Delete", "Active");
        Long id = mapper.readTree(json).get("id").asLong();

        mockMvc.perform(delete("/api/plans/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/plans/" + id))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/plans/search ──────────────────────────────────────────────

    @Test
    void search_returns200() throws Exception {
        mockMvc.perform(get("/api/plans/search?q=failover&status=Active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ── GET /api/plans/stats ───────────────────────────────────────────────

    @Test
    void stats_returns200_withTotalField() throws Exception {
        mockMvc.perform(get("/api/plans/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.active").exists())
                .andExpect(jsonPath("$.avgScore").exists());
    }

    // ── GET /api/plans/export ──────────────────────────────────────────────

    @Test
    void exportCsv_returns200_withCsvHeader() throws Exception {
        mockMvc.perform(get("/api/plans/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("continuity-plans.csv")));
    }

    // ── POST /api/auth/login ───────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        String body = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String body = "{\"username\":\"admin\",\"password\":\"wrong\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private ContinuityPlan buildPlan(String title, String status) {
        ContinuityPlan p = new ContinuityPlan();
        p.setTitle(title);
        p.setStatus(status);
        p.setDescription("Test description");
        return p;
    }

    private String createAndReturnBody(String title, String status) throws Exception {
        return mockMvc.perform(post("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(buildPlan(title, status))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
