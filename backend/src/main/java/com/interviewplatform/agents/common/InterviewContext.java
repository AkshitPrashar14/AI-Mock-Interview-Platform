package com.interviewplatform.agents.common;

import com.interviewplatform.interview.entity.DifficultyLevel;
import com.interviewplatform.interview.entity.InterviewState;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.interview.entity.Interview;
import com.interviewplatform.interview.entity.Question;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the entire context of an interview at a specific point in time.
 * Passed to all agents to eliminate multi-parameter method signatures.
 */
@Data
@Builder
public class InterviewContext {

    private Interview interview;
    private User candidate;
    private Question currentQuestion;
    private List<Question> previousQuestions;
    private String conversationHistory;
    private String transcript;
    
    private DifficultyLevel difficulty;
    private InterviewState state;

    private Map<String, Object> technicalResults;
    private Map<String, Object> englishResults;
    private Map<String, Object> behaviorResults;
    
    private Map<String, Object> metadata;
}
