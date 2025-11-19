package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualGradingRequest {
    private Long sessionId;
    private Long questionId;
    private Double score;
    private String feedback;
    private Long graderId;
}