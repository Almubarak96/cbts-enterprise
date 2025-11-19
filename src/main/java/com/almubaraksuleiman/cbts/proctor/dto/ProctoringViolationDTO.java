package com.almubaraksuleiman.cbts.proctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringViolationDTO {
    private Long id;
    private Long proctoringSessionId;
    private String studentName;
    private String examName;
    private LocalDateTime timestamp;
    private String violationType;
    private String severity;
    private Double confidence;
    private String description;
    private Boolean reviewed;
    private String screenshotUrl;
}
