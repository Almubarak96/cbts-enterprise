package com.almubaraksuleiman.cbts.examiner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestData {
    private Long id;
    private String title;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer numberOfQuestions;
    private Integer passingScore;
}