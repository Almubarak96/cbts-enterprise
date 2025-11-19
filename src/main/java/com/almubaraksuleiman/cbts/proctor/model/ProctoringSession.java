// Proctoring Session Entity
package com.almubaraksuleiman.cbts.proctor.model;

import com.almubaraksuleiman.cbts.student.model.StudentExam;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "proctoring_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctoringSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_exam_id", nullable = false)
    private StudentExam studentExam;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    private ProctoringStatus status;
    
    // Recording paths
    private String screenRecordingPath;
    private String videoRecordingPath;
    private String audioRecordingPath;
    
    // Status indicators
    private Boolean screenRecordingActive = false;
    private Boolean videoRecordingActive = false;
    private Boolean audioRecordingActive = false;
    
    // Monitoring metrics
    @Builder.Default
    private Integer suspiciousActivities = 0;
    private Integer connectionQuality = 100; // 0-100 percentage
    
    // Review flags
    private Boolean manuallyReviewed = false;
    private String reviewerComments;
    
    // Technical info
    private String browserInfo;
    private String ipAddress;
    private String operatingSystem;
}



