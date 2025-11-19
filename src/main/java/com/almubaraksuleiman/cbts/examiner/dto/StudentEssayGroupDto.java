package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEssayGroupDto {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private List<StudentEssayAnswerDto> essays;
    private int gradedCount;
    private int totalCount;
    private double completionPercentage;
}