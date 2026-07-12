package com.interviewplatform.report.repository;

import com.interviewplatform.report.entity.Report;
import com.interviewplatform.report.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    Optional<Report> findByInterviewId(UUID interviewId);

    List<Report> findByInterviewCandidateId(UUID candidateId);

    boolean existsByInterviewIdAndReportStatus(UUID interviewId, ReportStatus status);
}
