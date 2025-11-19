package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEssayAnswerDto {
    private Long sessionId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long questionId;
    private String questionText;
    private String essayAnswer;
    private Double maxMarks;
    private Double currentScore;
    private String graderFeedback;
    private LocalDateTime submittedAt;
    private boolean graded;
    private LocalDateTime gradedAt;
}