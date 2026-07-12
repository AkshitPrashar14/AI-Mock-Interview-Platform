package com.interviewplatform.interview.repository;

import com.interviewplatform.interview.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {

    List<Evaluation> findByInterviewIdOrderByEvaluatedAtAsc(UUID interviewId);

    Optional<Evaluation> findByAnswerId(UUID answerId);

    long countByInterviewId(UUID interviewId);
}
