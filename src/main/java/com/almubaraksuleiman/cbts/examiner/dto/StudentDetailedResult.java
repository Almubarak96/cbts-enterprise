package com.almubaraksuleiman.cbts.examiner.dto;

import com.almubaraksuleiman.cbts.dto.QuestionResult;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudentDetailedResult {
    private Long sessionId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String testTitle;
    private Integer score;
    private Double percentage;
    private Integer totalMarks;
    private Integer timeSpent;
    private String grade;
    private Boolean passed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    // Add question breakdown list here

    private List<QuestionResultDTO> questionResults;
}
