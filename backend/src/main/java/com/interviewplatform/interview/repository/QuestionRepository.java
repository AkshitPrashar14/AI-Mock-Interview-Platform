package com.interviewplatform.interview.repository;

import com.interviewplatform.interview.entity.Interview;
import com.interviewplatform.interview.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByInterviewIdOrderByQuestionNumberAsc(UUID interviewId);

    List<Question> findByInterviewOrderByQuestionNumberAsc(Interview interview);

    Optional<Question> findFirstByInterviewOrderByQuestionNumberDesc(Interview interview);

    Optional<Question> findByInterviewIdAndQuestionNumber(UUID interviewId, int questionNumber);

    long countByInterviewId(UUID interviewId);
}
