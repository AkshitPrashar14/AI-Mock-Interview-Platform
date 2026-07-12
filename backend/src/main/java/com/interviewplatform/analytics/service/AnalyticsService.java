package com.interviewplatform.analytics.service;

import com.interviewplatform.analytics.entity.AnalyticsSnapshot;
import com.interviewplatform.analytics.repository.AnalyticsSnapshotRepository;
import com.interviewplatform.interview.entity.PerformanceTier;
import com.interviewplatform.report.entity.Report;
import com.interviewplatform.report.entity.ReportStatus;
import com.interviewplatform.report.entity.Verdict;
import com.interviewplatform.report.repository.ReportRepository;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.user.exception.UserNotFoundException;
import com.interviewplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service to manage analytics snapshots.
 *
 * <p><b>Module:</b> Module 12 — Dashboard APIs</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsSnapshotRepository snapshotRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    /**
     * Recomputes and updates the analytics snapshot for a given candidate.
     * Call this when a new interview report is successfully generated.
     *
     * @param candidateId the user ID
     */
    @Transactional
    public void recomputeSnapshot(UUID candidateId) {
        log.info("AnalyticsService.recomputeSnapshot: candidateId={}", candidateId);

        List<Report> allReports = reportRepository.findByInterviewCandidateId(candidateId)
                .stream()
                .filter(r -> r.getReportStatus() == ReportStatus.COMPLETED)
                .toList();

        AnalyticsSnapshot snapshot = snapshotRepository.findByCandidateId(candidateId)
                .orElseGet(() -> {
                    User candidate = userRepository.findById(candidateId)
                            .orElseThrow(() -> new UserNotFoundException(candidateId.toString()));
                    return AnalyticsSnapshot.builder().candidate(candidate).build();
                });

        if (allReports.isEmpty()) {
            log.debug("No completed reports found for candidate, keeping empty snapshot.");
            snapshot.setLastComputedAt(Instant.now());
            snapshotRepository.save(snapshot);
            return;
        }

        int count = allReports.size();
        snapshot.setTotalInterviews(count);

        double totalTech = 0, totalEng = 0, totalBeh = 0, totalComp = 0;
        int maxComposite = -1;
        PerformanceTier bestTier = null;
        Verdict mostRecentVerdict = null;
        Instant mostRecentTime = Instant.MIN;

        for (Report r : allReports) {
            double tech = r.getFinalTechnicalScore() != null ? r.getFinalTechnicalScore().doubleValue() : 0;
            double eng = r.getFinalEnglishScore() != null ? r.getFinalEnglishScore().doubleValue() : 0;
            double beh = r.getFinalBehavioralScore() != null ? r.getFinalBehavioralScore().doubleValue() : 0;
            double comp = r.getFinalCompositeScore() != null ? r.getFinalCompositeScore().doubleValue() : 0;

            totalTech += tech;
            totalEng += eng;
            totalBeh += beh;
            totalComp += comp;

            if (comp > maxComposite) {
                maxComposite = (int) Math.round(comp);
                bestTier = r.getFinalTier();
            }

            if (r.getCreatedAt().isAfter(mostRecentTime)) {
                mostRecentTime = r.getCreatedAt();
                mostRecentVerdict = r.getVerdict();
            }
        }

        snapshot.setAvgTechnicalScore(BigDecimal.valueOf(totalTech / count).setScale(2, RoundingMode.HALF_UP));
        snapshot.setAvgEnglishScore(BigDecimal.valueOf(totalEng / count).setScale(2, RoundingMode.HALF_UP));
        snapshot.setAvgBehavioralScore(BigDecimal.valueOf(totalBeh / count).setScale(2, RoundingMode.HALF_UP));
        snapshot.setAvgCompositeScore(BigDecimal.valueOf(totalComp / count).setScale(2, RoundingMode.HALF_UP));
        snapshot.setBestPerformanceTier(bestTier);
        snapshot.setMostRecentVerdict(mostRecentVerdict);

        // Find most practiced domain
        String mostPracticed = allReports.stream()
                .collect(Collectors.groupingBy(r -> r.getInterview().getDomain(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        snapshot.setMostPracticedDomain(mostPracticed);

        snapshot.setLastComputedAt(Instant.now());
        snapshotRepository.save(snapshot);

        log.info("AnalyticsService: snapshot updated for candidateId={}. Total interviews={}", candidateId, count);
    }

    /**
     * Gets the analytics snapshot for a candidate. If it doesn't exist, returns an empty one.
     */
    @Transactional(readOnly = true)
    public AnalyticsSnapshot getSnapshot(UUID candidateId) {
        return snapshotRepository.findByCandidateId(candidateId)
                .orElseGet(() -> AnalyticsSnapshot.builder()
                        .totalInterviews(0)
                        .avgTechnicalScore(BigDecimal.ZERO)
                        .avgEnglishScore(BigDecimal.ZERO)
                        .avgBehavioralScore(BigDecimal.ZERO)
                        .avgCompositeScore(BigDecimal.ZERO)
                        .build()); // Return unpersisted dummy for empty state
    }
}
