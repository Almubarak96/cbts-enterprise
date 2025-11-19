// Dashboard DTOs
package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private int totalTests;
    private int totalStudents;
    private int totalExaminers;
    private int activeTests;
    private int completedExams;
    private double averageScore;
    private double passRate;
    private double enrollmentRate;
    private int pendingGrading;
    private int totalQuestions;
}

