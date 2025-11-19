package com.almubaraksuleiman.cbts.examiner.model;

import com.almubaraksuleiman.cbts.student.model.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_id", "student_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    @JsonIgnore
    private Test test;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @CreationTimestamp
    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;
    
    @Column(name = "notification_sent")
    private Boolean notificationSent = false;
    
    public enum EnrollmentStatus {
        ENROLLED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}