package com.interviewplatform.agents.report;

import com.interviewplatform.agents.aggregator.AggregatedEvaluation;
import com.interviewplatform.interview.entity.Evaluation;
import com.interviewplatform.interview.entity.PerformanceTier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportAggregator {

    public AggregatedEvaluation aggregateFinalScore(List<Evaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return AggregatedEvaluation.builder()
                    .technicalScore(0)
                    .englishScore(0)
                    .behavioralScore(0)
                    .compositeScore(0)
                    .performanceTier(PerformanceTier.NEEDS_WORK.name())
                    .build();
        }

        int totalTech = 0;
        int totalEng = 0;
        int totalBeh = 0;
        int totalComp = 0;

        for (Evaluation eval : evaluations) {
            totalTech += eval.getTechnicalScore();
            totalEng += eval.getEnglishScore();
            totalBeh += eval.getBehavioralScore();
            totalComp += eval.getCompositeScore();
        }

        int count = evaluations.size();
        int finalTech = totalTech / count;
        int finalEng = totalEng / count;
        int finalBeh = totalBeh / count;
        int finalComp = totalComp / count;

        return AggregatedEvaluation.builder()
                .technicalScore(finalTech)
                .englishScore(finalEng)
                .behavioralScore(finalBeh)
                .compositeScore(finalComp)
                .performanceTier(PerformanceTier.fromScore(finalComp).name())
                .build();
    }
}
