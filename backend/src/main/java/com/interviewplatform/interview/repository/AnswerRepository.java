package com.interviewplatform.interview.repository;

import com.interviewplatform.interview.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    List<Answer> findByInterviewIdOrderByCreatedAtAsc(UUID interviewId);

    Optional<Answer> findByInterviewIdAndQuestionId(UUID interviewId, UUID questionId);

    long countByInterviewId(UUID interviewId);
}
