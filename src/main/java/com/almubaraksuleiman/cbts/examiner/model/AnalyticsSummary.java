package com.almubaraksuleiman.cbts.examiner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummary {
    private int totalStudents;
    private int completedStudents;
    private double averageScore;
    private double passRate;
    private double averageTimeSpent;
}