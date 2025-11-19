// Violation Event Entity
package com.almubaraksuleiman.cbts.proctor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "proctoring_violations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctoringViolation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proctoring_session_id", nullable = false)
    private ProctoringSession proctoringSession;
    
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    private ViolationType violationType;
    
    @Enumerated(EnumType.STRING)
    private Severity severity;
    
    private Double confidence; // AI confidence score 0-1
    private String screenshotPath;
    private String description;
    private String metadata; // JSON string for additional data
    
    private Boolean reviewed = false;
    private String reviewComments;
    private LocalDateTime reviewedAt;
}



