package com.almubaraksuleiman.cbts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamSessionDto {
    private Long sessionId;
    private Long studentId;
    private Long testId;
    private Integer totalQuestions;
    private LocalDateTime startTime;
    private int durationMinutes; // NEW FIELD
    private String status;
    private Integer currentQuestionIndex;


}