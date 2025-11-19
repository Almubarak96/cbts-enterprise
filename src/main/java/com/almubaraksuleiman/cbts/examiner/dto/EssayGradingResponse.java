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
public class EssayGradingResponse {
    private Long sessionId;
    private Long questionId;
    private Double awardedScore;
    private Double maxMarks;
    private String feedback;
    private LocalDateTime gradedAt;
    private String graderName;
    private boolean success;
    private String message;
}