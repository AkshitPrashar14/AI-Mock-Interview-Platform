package com.interviewplatform.interview.mapper;

import com.interviewplatform.interview.dto.response.InterviewResponse;
import com.interviewplatform.interview.dto.response.InterviewSummaryResponse;
import com.interviewplatform.interview.entity.Interview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import com.interviewplatform.interview.entity.Question;
import com.interviewplatform.interview.entity.Answer;
import java.util.Comparator;

/**
 * MapStruct mapper for {@link Interview} → response DTOs.
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "interviewId", source = "id")
    @Mapping(target = "currentQuestionText", ignore = true)
    @Mapping(target = "lastEvaluationFeedback", ignore = true)
    InterviewResponse toResponse(Interview interview);

    InterviewSummaryResponse toSummaryResponse(Interview interview);

    @AfterMapping
    default void populateExtraFields(Interview interview, @MappingTarget InterviewResponse.InterviewResponseBuilder builder) {
        if (interview.getQuestions() != null && !interview.getQuestions().isEmpty() && interview.getCurrentQuestionNumber() != null) {
            interview.getQuestions().stream()
                .filter(q -> q.getQuestionNumber().equals(interview.getCurrentQuestionNumber()))
                .findFirst()
                .ifPresent(q -> builder.currentQuestionText(q.getQuestionText()));

            // Find feedback for previous question if current is > 1
            if (interview.getCurrentQuestionNumber() > 1) {
                interview.getQuestions().stream()
                    .filter(q -> q.getQuestionNumber().equals(interview.getCurrentQuestionNumber() - 1))
                    .findFirst()
                    .map(Question::getAnswer)
                    .map(Answer::getEvaluation)
                    .ifPresent(eval -> builder.lastEvaluationFeedback(eval.getFeedback()));
            }
        }
    }
}
