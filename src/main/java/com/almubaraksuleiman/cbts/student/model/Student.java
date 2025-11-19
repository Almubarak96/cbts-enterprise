package com.almubaraksuleiman.cbts.student.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Student Entity representing a user who takes exams in the Computer-Based Test system.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "student")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username cannot exceed 50 characters")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(nullable = false)
    private String firstName;

    @Column
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    /**
     * URL to the student's profile picture
     * Stored in cloud storage or local file system
     */
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    /**
     * URL to thumbnail version of profile picture for faster loading
     * Used in UI components like exam session header
     */
    @Column(name = "profile_picture_thumbnail_url")
    private String profilePictureThumbnailUrl;

    /**
     * Student account status for lifecycle management
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    /**
     * Audit field - when the student account was created
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Audit field - when the student account was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing student account status
     */
    public enum StudentStatus {
        ACTIVE, INACTIVE, SUSPENDED, GRADUATED, WITHDRAWN
    }

    /**
     * Pre-persist callback to set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Pre-update callback to set update timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the full name of the student for display purposes
     *
     * @return Formatted full name string
     */
    public String getFullName() {
        if (middleName != null && !middleName.trim().isEmpty()) {
            return String.format("%s %s %s", firstName, middleName, lastName).trim();
        }
        return String.format("%s %s", firstName, lastName).trim();
    }

    /**
     * Returns display name for UI components
     *
     * @return Formatted display name
     */
    public String getDisplayName() {
        return getFullName();
    }

    /**
     * Gets profile picture URL with fallback to default image
     *
     * @return Profile picture URL string
     */
    public String getProfilePictureUrl() {
        return this.profilePictureUrl != null ? this.profilePictureUrl : "/assets/images/default-profile.png";
    }

    /**
     * Gets thumbnail URL with fallback to full image or default
     *
     * @return Thumbnail URL string
     */
    public String getProfilePictureThumbnailUrl() {
        return this.profilePictureThumbnailUrl != null ?
                this.profilePictureThumbnailUrl : getProfilePictureUrl();
    }

    @Override
    public String toString() {
        return String.format("Student{id=%d, username='%s', name='%s', email='%s', status=%s}",
                id, username, getFullName(), email, status);
    }
}