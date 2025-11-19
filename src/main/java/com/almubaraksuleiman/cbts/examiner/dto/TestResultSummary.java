package com.almubaraksuleiman.cbts.examiner.dto;

import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TestResultSummary {
    private Long testId;
    private String testTitle;
    private Integer totalStudents;
    private Integer completedStudents;
    private Integer gradedStudents;
    private Double averageScore;
    private Double passRate;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer passingScore;
    private Boolean published;
    private LocalDateTime createdAt;
    private Examiner createdBy;
}
