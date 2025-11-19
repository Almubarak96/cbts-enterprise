package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentResultsFilter {
    private String searchTerm;
    private Double minScore;
    private Double maxScore;
    private String status;
    private Boolean graded;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
