package com.almubaraksuleiman.cbts.examiner.dto;

import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.almubaraksuleiman.cbts.student.model.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Data

public class EnrollmentDto {

    private Long id;

    private Test test;

    private Student student;

    private LocalDateTime enrolledAt;

    private com.almubaraksuleiman.cbts.examiner.model.Enrollment.EnrollmentStatus status;

    private Boolean notificationSent = false;

}