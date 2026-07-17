package com.interviewplatform.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewplatform.interview.controller.InterviewController;
import com.interviewplatform.interview.dto.request.CreateInterviewRequest;
import com.interviewplatform.interview.dto.response.InterviewResponse;
import com.interviewplatform.interview.dto.response.InterviewStartResponse;
import com.interviewplatform.interview.dto.response.InterviewSummaryResponse;
import com.interviewplatform.interview.entity.*;
import com.interviewplatform.interview.exception.InterviewAlreadyStartedException;
import com.interviewplatform.interview.exception.InterviewNotFoundException;
import com.interviewplatform.interview.exception.InvalidStateTransitionException;
import com.interviewplatform.interview.service.InterviewService;
import com.interviewplatform.orchestrator.InterviewOrchestrator;
import com.interviewplatform.user.entity.Role;
import com.interviewplatform.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc slice tests for {@link InterviewController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the web layer.
 * The authenticated {@link User} principal is injected via
 * {@link SecurityMockMvcRequestPostProcessors#user(String)}.</p>
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@WebMvcTest(InterviewController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.jwt.secret=dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1mb3ItdGVzdHMhISE=",
    "app.jwt.issuer=test-platform",
    "app.jwt.audience=test-clients",
    "app.jwt.access-token-expiration-ms=900000",
    "app.jwt.refresh-token-expiration-days=7",
    "app.jwt.token-version=1"
})
@DisplayName("InterviewController")
class InterviewControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean InterviewService interviewService;
    @MockBean InterviewOrchestrator interviewOrchestrator;

    private User principal;
    private UUID candidateId;
    private UUID interviewId;

    @BeforeEach
    void setUp() {
        candidateId  = UUID.randomUUID();
        interviewId  = UUID.randomUUID();

        principal = User.builder()
                .id(candidateId)
                .firstName("Jane")
                .lastName("Dev")
                .email("jane@example.com")
                .passwordHash("$2a$10$hash")
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    // =========================================================================
    // POST /api/v1/interviews — Create
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/interviews")
    class CreateInterviewTests {

        @Test
        @DisplayName("201 Created — valid request body creates interview")
        void createInterview_success() throws Exception {
            CreateInterviewRequest req = new CreateInterviewRequest();
            req.setDomain("Java Backend");
            req.setRoleLevel(RoleLevel.SENIOR);
            req.setTotalQuestions(10);
            req.setStartingDifficulty(DifficultyLevel.MEDIUM);

            InterviewResponse response = InterviewResponse.builder()
                    .interviewId(interviewId)
                    .state(InterviewState.CREATED)
                    .domain("Java Backend")
                    .roleLevel(RoleLevel.SENIOR)
                    .build();

            when(interviewService.createInterview(any(), eq(candidateId))).thenReturn(response);

            mockMvc.perform(post("/api/v1/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(SecurityMockMvcRequestPostProcessors.user(principal))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.interviewId").value(interviewId.toString()))
                    .andExpect(jsonPath("$.data.state").value("CREATED"));
        }

        @Test
        @DisplayName("400 Bad Request — missing required domain field")
        void createInterview_missingDomain() throws Exception {
            CreateInterviewRequest req = new CreateInterviewRequest();
            req.setRoleLevel(RoleLevel.JUNIOR);
            req.setTotalQuestions(5);
            req.setStartingDifficulty(DifficultyLevel.EASY);
            // domain not set

            mockMvc.perform(post("/api/v1/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(SecurityMockMvcRequestPostProcessors.user(principal))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // POST /api/v1/interviews/{id}/start — Start
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/interviews/{id}/start")
    class StartInterviewTests {

        @Test
        @DisplayName("200 OK — starts interview and returns first question")
        void startInterview_success() throws Exception {
            InterviewStartResponse response = InterviewStartResponse.builder()
                    .interviewId(interviewId)
                    .state(InterviewState.WAITING_FOR_RESPONSE)
                    .question(InterviewStartResponse.QuestionDetail.builder()
                            .id(UUID.randomUUID())
                            .number(1)
                            .text("Introduce yourself")
                            .type(QuestionType.BEHAVIORAL)
                            .difficulty(DifficultyLevel.MEDIUM)
                            .totalQuestions(10)
                            .build())
                    .build();

            when(interviewService.startInterview(eq(interviewId), eq(candidateId))).thenReturn(response);

            mockMvc.perform(post("/api/v1/interviews/{id}/start", interviewId)
                            .with(SecurityMockMvcRequestPostProcessors.user(principal))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("WAITING_FOR_RESPONSE"))
                    .andExpect(jsonPath("$.data.question.number").value(1));
        }

        @Test
        @DisplayName("409 Conflict — interview already started")
        void startInterview_alreadyStarted() throws Exception {
            when(interviewService.startInterview(eq(interviewId), eq(candidateId)))
                    .thenThrow(new InterviewAlreadyStartedException());

            mockMvc.perform(post("/api/v1/interviews/{id}/start", interviewId)
                            .with(SecurityMockMvcRequestPostProcessors.user(principal))
                            .with(csrf()))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("404 Not Found — interview not found or not owned")
        void startInterview_notFound() throws Exception {
            when(interviewService.startInterview(eq(interviewId), eq(candidateId)))
                    .thenThrow(new InterviewNotFoundException(interviewId));

            mockMvc.perform(post("/api/v1/interviews/{id}/start", interviewId)
                            .with(SecurityMockMvcRequestPostProcessors.user(principal))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/v1/interviews/{id} — Get
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/interviews/{id}")
    class GetInterviewTests {

        @Test
        @DisplayName("200 OK — returns interview state")
        void getInterview_success() throws Exception {
            InterviewResponse response = InterviewResponse.builder()
                    .interviewId(interviewId)
                    .state(InterviewState.WAITING_FOR_RESPONSE)
                    .domain("Python ML")
                    .build();

            when(interviewService.getInterview(eq(interviewId), eq(candidateId))).thenReturn(response);

            mockMvc.perform(get("/api/v1/interviews/{id}", interviewId)
                            .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.interviewId").value(interviewId.toString()))
                    .andExpect(jsonPath("$.data.domain").value("Python ML"));
        }

        @Test
        @DisplayName("404 Not Found — interview not owned by caller")
        void getInterview_notOwned() throws Exception {
            when(interviewService.getInterview(eq(interviewId), eq(candidateId)))
                    .thenThrow(new InterviewNotFoundException(interviewId));

            mockMvc.perform(get("/api/v1/interviews/{id}", interviewId)
                            .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/v1/interviews — List
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/interviews")
    class ListInterviewTests {

        @Test
        @DisplayName("200 OK — returns paged list of interviews")
        void listInterviews_success() throws Exception {
            InterviewSummaryResponse summary = InterviewSummaryResponse.builder()
                    .interviewId(interviewId)
                    .state(InterviewState.COMPLETED)
                    .domain("Java Backend")
                    .build();

            Page<InterviewSummaryResponse> page = new PageImpl<>(List.of(summary));
            when(interviewService.listInterviews(eq(candidateId), isNull(), any())).thenReturn(page);

            mockMvc.perform(get("/api/v1/interviews")
                            .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].interviewId").value(interviewId.toString()));
        }

        @Test
        @DisplayName("200 OK — returns filtered list by state")
        void listInterviews_withStateFilter() throws Exception {
            Page<InterviewSummaryResponse> page = new PageImpl<>(List.of());
            when(interviewService.listInterviews(eq(candidateId), eq(InterviewState.COMPLETED), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/interviews")
                            .param("state", "COMPLETED")
                            .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    // =========================================================================
    // POST /api/v1/interviews/{id}/end — End
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/interviews/{id}/end")
    class EndInterviewTests {

        @Test
        @DisplayName("200 OK — ends interview successfully")
        void endInterview_success() throws Exception {
            InterviewResponse response = InterviewResponse.builder()
                    .interviewId(interviewId)
                    .state(InterviewState.COMPLETED)
                    .build();

            when(interviewService.endInterview(eq(interviewId), eq(candidateId))).thenReturn(response);

            mockMvc.perform(post("/api/v1/interviews/{id}/end", interviewId)
                            .with(SecurityMockMvcRequestPostProcessors.user(principal))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.state").value("COMPLETED"));
        }

        @Test
        @DisplayName("409 Conflict — interview in terminal state")
        void endInterview_terminalState() throws Exception {
            when(interviewService.endInterview(eq(interviewId), eq(candidateId)))
                    .thenThrow(new InvalidStateTransitionException("Interview is already in a terminal state: REPORT_GENERATED"));

            mockMvc.perform(post("/api/v1/interviews/{id}/end", interviewId)
                            .with(SecurityMockMvcRequestPostProcessors.user(principal))
                            .with(csrf()))
                    .andExpect(status().isConflict());
        }
    }
}
