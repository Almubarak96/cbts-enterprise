package com.almubaraksuleiman.cbts.student.model;

import com.almubaraksuleiman.cbts.examiner.model.Test;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Represents a student's exam session in the Computer-Based Test (CBT) system.
 * Enhanced with better timing tracking, status management, and relationship mapping.
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "student_exam")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_id")
    private Student student;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "completed")
    @Builder.Default
    private Boolean completed = false;

    @Column(name = "score")
    private Integer score;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "graded")
    @Builder.Default
    private Boolean graded = false;

    /**
     * Enhanced status tracking with enum support
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ExamStatus status = ExamStatus.NOT_STARTED;

    /**
     * Time spent on exam in seconds for accurate tracking
     */
    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Long timeSpentSeconds = 0L;

    /**
     * Current question index for resume functionality
     */
    @Column(name = "current_question_index")
    @Builder.Default
    private Integer currentQuestionIndex = 0;

    /**
     * Audit fields
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing comprehensive exam session status
     */
    public enum ExamStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        SUBMITTED,
        TIMED_OUT,
        CANCELLED,
        UNDER_REVIEW,
        PARTIALLY_GRADED,
        FULLY_GRADED,
        GRADED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates the time spent on the exam
     *
     * @return Duration object representing time spent
     */
    public Duration getTimeSpent() {
        if (startTime == null) {
            return Duration.ZERO;
        }

        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }

    /**
     * Calculates remaining time for the exam
     *
     * @return Duration object representing remaining time
     */
    public Duration getRemainingTime() {
        if (startTime == null || test == null || test.getDurationMinutes() == null) {
            return Duration.ZERO;
        }

        LocalDateTime expectedEnd = startTime.plusMinutes(test.getDurationMinutes());
        return Duration.between(LocalDateTime.now(), expectedEnd);
    }

    /**
     * Checks if exam is currently in progress
     *
     * @return true if exam is active and not completed
     */
    public boolean isInProgress() {
        return status == ExamStatus.IN_PROGRESS &&
                startTime != null &&
                !Boolean.TRUE.equals(completed);
    }

    /**
     * Checks if exam time has expired
     *
     * @return true if time limit has been exceeded
     */
    public boolean isTimeExpired() {
        if (startTime == null || test == null || test.getDurationMinutes() == null) {
            return false;
        }

        LocalDateTime expirationTime = startTime.plusMinutes(test.getDurationMinutes());
        return LocalDateTime.now().isAfter(expirationTime);
    }

    /**
     * Starts the exam session
     */
    public void startExam() {
        this.startTime = LocalDateTime.now();
        this.status = ExamStatus.IN_PROGRESS;
        this.completed = false;
        this.currentQuestionIndex = 0;
    }

    /**
     * Completes the exam session
     */
    public void completeExam() {
        this.endTime = LocalDateTime.now();
        this.completed = true;
        this.status = ExamStatus.COMPLETED;
        this.timeSpentSeconds = getTimeSpent().getSeconds();
    }

    /**
     * Submits the exam for grading
     */
    public void submitExam() {
        this.endTime = LocalDateTime.now();
        this.completed = true;
        this.status = ExamStatus.SUBMITTED;
        this.timeSpentSeconds = getTimeSpent().getSeconds();
    }

    @Override
    public String toString() {
        return String.format("StudentExam{sessionId=%d, studentId=%d, test=%s, status=%s}",
                sessionId, studentId, test != null ? test.getTitle() : "null", status);
    }


    /*
     * Potential additional fields that could be added:

     * // Time limit for this specific exam (could override test duration)
     * private Integer timeLimitMinutes;

     * // Time spent on the exam (calculated from startTime and endTime)
     * private Integer timeSpentMinutes;

     * // IP address from which the exam was taken (for security)
     * private String ipAddress;

     * // Browser/user agent information
     * private String userAgent;

     * // Number of times the student paused the exam
     * private Integer pauseCount;

     * // Flag for suspected cheating or irregularities
     * private Boolean flaggedForReview;

     * // Comments from the grader (for manual grading)
     * private String graderComments;

     * // Timestamp when the exam was graded
     * private LocalDateTime gradedTime;

     * // Grade letter (A, B, C, etc.) based on percentage
     * private String gradeLetter;
     */

    /*
     * Helper methods that could be added:

     * // Calculate time spent on exam
     * public Integer getTimeSpentMinutes() {
     *     if (startTime != null && endTime != null) {
     *         return (int) java.time.Duration.between(startTime, endTime).toMinutes();
     *     }
     *     return null;
     * }

     * // Check if exam is currently in progress
     * public boolean isInProgress() {
     *     return startTime != null && endTime == null && !Boolean.TRUE.equals(completed);
     * }

     * // Check if exam time has expired (if test has duration)
     * public boolean isTimeExpired() {
     *     if (startTime != null && test != null && test.getDurationMinutes() != null) {
     *         LocalDateTime expirationTime = startTime.plusMinutes(test.getDurationMinutes());
     *         return LocalDateTime.now().isAfter(expirationTime);
     *     }
     *     return false;
     * }
     */

    /*
     * Example of builder pattern usage:
     * StudentExam examSession = StudentExam.builder()
     *     .studentId(123L)
     *     .test(mathTest)
     *     .startTime(LocalDateTime.now())
     *     .completed(false)
     *     .graded(false)
     *     .status("IN_PROGRESS")
     *     .build();
     */
}