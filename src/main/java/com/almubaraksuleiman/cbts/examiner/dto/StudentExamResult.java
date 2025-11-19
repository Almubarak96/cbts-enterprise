package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentExamResult {
    private Long sessionId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Integer score;
    private Double percentage;
    private Integer timeSpent; // minutes
    private String status;
    private Boolean graded;
    private Boolean completed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String grade;
    private Boolean passed;
}
