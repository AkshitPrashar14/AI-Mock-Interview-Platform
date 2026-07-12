package com.interviewplatform.interview.dto.response;

import com.interviewplatform.interview.entity.DifficultyLevel;
import com.interviewplatform.interview.entity.InterviewState;
import com.interviewplatform.interview.entity.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response to {@code POST /api/v1/interviews/{id}/start}.
 *
 * <p>Returns the interview state and the first question to display to the candidate.</p>
 */
@Data
@Builder
@Schema(description = "Interview start response — includes first question")
public class InterviewStartResponse {

    @Schema(description = "Interview session ID")
    private UUID interviewId;

    @Schema(description = "New state after starting")
    private InterviewState state;

    @Schema(description = "The first question delivered to the candidate")
    private QuestionDetail question;

    @Data
    @Builder
    @Schema(description = "A single question delivered to the candidate")
    public static class QuestionDetail {
        private UUID id;
        private Integer number;
        private String text;
        private QuestionType type;
        private DifficultyLevel difficulty;
        private Integer totalQuestions;
    }
}
