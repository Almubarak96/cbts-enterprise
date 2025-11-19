package com.almubaraksuleiman.cbts.examiner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnalysis {
    private int questionId;
    private String text;
    private int correctAnswers;
    private int incorrectAnswers;
    private double averageTime;
    private String difficulty; // "easy", "medium", "hard"
}