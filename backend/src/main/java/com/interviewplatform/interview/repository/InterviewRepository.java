package com.interviewplatform.interview.repository;

import com.interviewplatform.interview.entity.Interview;
import com.interviewplatform.interview.entity.InterviewState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Interview} entities.
 *
 * <p><b>Module:</b> Module 2 — Interview Session Management</p>
 */
@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    /**
     * Finds an interview by ID, eagerly loading the candidate to avoid N+1.
     */
    @Query("SELECT i FROM Interview i JOIN FETCH i.candidate WHERE i.id = :id")
    Optional<Interview> findByIdWithCandidate(@Param("id") UUID id);

    /**
     * Lists all interviews for a given candidate, ordered by creation date descending.
     */
    Page<Interview> findByCandidateIdOrderByCreatedAtDesc(UUID candidateId, Pageable pageable);

    /**
     * Lists interviews by candidate and state filter.
     */
    Page<Interview> findByCandidateIdAndStateOrderByCreatedAtDesc(
            UUID candidateId, InterviewState state, Pageable pageable);

    /**
     * Lists active interviews for a candidate (excluding specified states).
     */
    java.util.List<Interview> findByCandidateIdAndStateNotInOrderByCreatedAtDesc(
            UUID candidateId, java.util.Collection<InterviewState> states);

    /**
     * Counts total interviews for a candidate.
     */
    long countByCandidateId(UUID candidateId);

    /**
     * Checks if an interview belongs to a specific candidate.
     */
    boolean existsByIdAndCandidateId(UUID interviewId, UUID candidateId);
}
