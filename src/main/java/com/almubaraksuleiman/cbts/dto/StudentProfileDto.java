package com.almubaraksuleiman.cbts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for student profile information
 * Used to send student data to the frontend for display in exam session
 * 
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileDto {
    private Long id;
    private String username;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String department;
    private String profilePictureUrl;
    private String profilePictureThumbnailUrl;
    private boolean verified;
    private String status;

    /**
     * Gets display name for UI
     */
    public String getDisplayName() {
        if (middleName != null && !middleName.trim().isEmpty()) {
            return String.format("%s %s %s", firstName, middleName, lastName).trim();
        }
        return String.format("%s %s", firstName, lastName).trim();
    }

    /**
     * Gets profile picture URL with fallback
     */
    public String getProfilePictureUrl() {
        return this.profilePictureUrl != null ? this.profilePictureUrl : "/assets/images/default-profile.png";
    }

    /**
     * Gets thumbnail URL with fallback
     */
    public String getProfilePictureThumbnailUrl() {
        return this.profilePictureThumbnailUrl != null ? this.profilePictureThumbnailUrl : getProfilePictureUrl();
    }
}