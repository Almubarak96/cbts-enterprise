package com.almubaraksuleiman.cbts.examiner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsData {
    private TestData test;
    private AnalyticsSummary summary;
    private List<ScoreDistribution> scoreDistribution;
    private List<QuestionAnalysis> questionAnalysis;
    private List<TimeAnalysis> timeAnalysis;
    private List<StudentPerformance> studentPerformance;
}