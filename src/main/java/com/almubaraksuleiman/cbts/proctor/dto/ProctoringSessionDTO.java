// DTOs for Proctoring
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
public class ProctoringSessionDTO {
    private Long id;
    private Long studentExamId;
    private Long studentId;
    private String studentName;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Boolean screenRecordingActive;
    private Boolean videoRecordingActive;
    private Boolean audioRecordingActive;
    private Integer suspiciousActivities;
    private Integer connectionQuality;
}


