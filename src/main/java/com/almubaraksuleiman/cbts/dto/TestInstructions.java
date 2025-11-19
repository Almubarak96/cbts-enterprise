package com.almubaraksuleiman.cbts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestInstructions implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long testId;
    private String examName;
    private List<String> instructions;
    private int durationMinutes;
    private int totalQuestions;
    private int passingScore;
    //private LocalDateTime createdAt;
    //private LocalDateTime updatedAt;
    private boolean shuffleQuestions;
    private boolean shuffleChoices;
    private boolean showResultsImmediately;

    // Track which users have read these instructions
    private List<String> readBy;
}