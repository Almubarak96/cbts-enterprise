package com.almubaraksuleiman.cbts.examiner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPerformance {
    private String studentId;
    private String name;
    private double score;
    private double timeSpent;
    private String status; // "completed", "in-progress", "not-started"
    private LocalDateTime submittedAt;
}