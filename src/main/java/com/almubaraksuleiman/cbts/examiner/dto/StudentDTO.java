package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private Long id;
    private String name;
    private String email;
    private String studentId;
    private String department;
    private Boolean enrolled;
    private String enrollmentDate;
}
