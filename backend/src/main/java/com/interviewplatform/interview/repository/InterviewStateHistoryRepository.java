package com.interviewplatform.interview.repository;

import com.interviewplatform.interview.entity.InterviewStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewStateHistoryRepository extends JpaRepository<InterviewStateHistory, UUID> {

    List<InterviewStateHistory> findByInterviewIdOrderByTransitionedAtAsc(UUID interviewId);
}
