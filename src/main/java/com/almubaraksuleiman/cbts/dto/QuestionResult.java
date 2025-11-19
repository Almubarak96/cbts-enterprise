package com.almubaraksuleiman.cbts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResult {
    private Long questionId;
    private String questionText;
    private String questionType;
    private String studentAnswer;
    private String correctAnswer;
    private Double score;
    private Double maxMarks;
    private Boolean isCorrect;
}