package com.interviewplatform.feedback.controller;

import com.interviewplatform.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/feedback")
@Tag(name = "Feedback", description = "Platform Feedback API")
public class FeedbackController {

    @PostMapping
    @Operation(summary = "Submit platform feedback", description = "Submit feedback regarding the platform experience.")
    public ApiResponse<Void> submitFeedback(@RequestBody FeedbackRequest request) {
        log.info("Received platform feedback: rating={}, message='{}'", request.getRating(), request.getMessage());
        return ApiResponse.success(null);
    }

    @Data
    public static class FeedbackRequest {
        private Integer rating;
        private String message;
    }
}
