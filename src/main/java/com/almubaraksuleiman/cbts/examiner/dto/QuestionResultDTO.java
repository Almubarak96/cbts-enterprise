package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResultDTO {
    private Long questionId;
    private String questionText;
    private String questionType;
    private String studentAnswer;
    private String correctAnswer;
    private Double score;
    private Double maxMarks;
    private Boolean isCorrect;
}