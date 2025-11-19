// Package declaration - organizes classes within the DTO package
package com.almubaraksuleiman.cbts.dto;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Enhanced Test Data Transfer Object (DTO) with comprehensive test window configuration.
 *
 * This DTO represents the test data transferred between client and server layers.
 * It includes all test properties including the new test window management features,
 * access controls, and calculated status fields.
 *
 * The DTO pattern is used to:
 * - Decouple the internal entity structure from external API
 * - Control which fields are exposed to clients
 * - Provide calculated fields that aren't persisted
 * - Format dates and handle serialization/deserialization
 *
 * @author Almubarak Suleiman
 * @version 2.0
 * @since 2025
 */
@Data // Lombok: generates getters, setters, toString, equals, and hashCode methods
public class TestDto {

    /**
     * Unique identifier for the test.
     * Matches the ID from the Test entity.
     * Used for all update, retrieval, and reference operations.
     */
    private Long id;

    /**
     * Name or title of the test.
     * Required field for all tests.
     * Displayed in test lists and during test taking.
     */
    private String title;

    /**
     * Number of questions to present to each student.
     * Optional field - if null, all questions are presented.
     * Used for creating randomized test versions.
     */
    private Integer numberOfQuestions;

    /**
     * Flag indicating if questions should be randomized.
     * Enhances test security by preventing question sequence sharing.
     * Defaults to false if not specified.
     */
    private Boolean randomizeQuestions;

    /**
     * Flag indicating if answer choices should be shuffled.
     * Prevents answer pattern memorization and sharing.
     * Defaults to false if not specified.
     */
    private Boolean shuffleChoices;

    /**
     * Detailed description and instructions for the test.
     * Supports HTML formatting for rich content.
     * Displayed to students before test begins.
     */
    private String description;

    /**
     * Time limit for completing the test in minutes.
     * Required field for all timed tests.
     * Used with timeEnforcement to control test duration.
     */
    private Integer durationMinutes;

    /**
     * Total maximum marks available in the test.
     * Required field for all tests.
     * Used for score calculation and percentage computation.
     */
    private Integer totalMarks;

    /**
     * Minimum score required to pass the test.
     * Absolute value (not percentage).
     * Used to determine pass/fail status.
     */
    private Integer passingScore;

    /**
     * Publication status of the test.
     * Controls visibility to students.
     * Defaults to false (unpublished) when creating new tests.
     */
    private Boolean published;

    // New Test Window Configuration Fields

    /**
     * Scheduled start date and time for test availability.
     * Formatted as ISO datetime for consistent client-server communication.
     * If null, test is available immediately upon publication.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") // ISO datetime format
    private LocalDateTime scheduledStartTime;

    /**
     * Scheduled end date and time for test availability.
     * Formatted as ISO datetime for consistent client-server communication.
     * If null, test remains available indefinitely.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") // ISO datetime format
    private LocalDateTime scheduledEndTime;

    /**
     * Time enforcement mode for the test duration.
     * Controls how the system handles time expiration.
     * Maps to the TimeEnforcementMode enum from Test entity.
     */
    private Test.TimeEnforcementMode timeEnforcement;

    /**
     * Maximum number of attempts allowed for this test.
     * Null or 0 indicates unlimited attempts.
     * Used to prevent test retake abuse.
     */
    private Integer maxAttempts;

    /**
     * Buffer time in minutes before scheduled start for early access.
     * Allows students to join and prepare before official start time.
     * Defaults to 0 if not specified.
     */
    private Integer startBufferMinutes;

    /**
     * Buffer time in minutes after scheduled end for late submissions.
     * Accommodates network delays and technical issues.
     * Defaults to 0 if not specified.
     */
    private Integer endBufferMinutes;

    /**
     * IP address restrictions for test access.
     * Comma-separated list of IPs, ranges, or patterns.
     * Empty or null means no IP restrictions.
     */
    private String allowedIPs;

    /**
     * Secure browser requirement flag.
     * If true, requires special browser with disabled developer tools.
     * Used for high-security testing environments.
     */
    private Boolean secureBrowser;

    // Calculated Fields (Read-only)

    /**
     * Current status of the test calculated based on schedule and settings.
     * This is a read-only field computed on the server.
     * Used for display purposes and client-side logic.
     * Possible values: DRAFT, SCHEDULED, ACTIVE, EXPIRED
     */
    private Test.TestStatus currentStatus;

    /**
     * Flag indicating if the test is currently accessible to students.
     * This is a read-only field computed on the server.
     * Considers publication status, schedule, and buffer times.
     * Used to control test start buttons and access permissions.
     */
    private Boolean currentlyAccessible;

    // Audit Fields (Read-only)

    /**
     * Timestamp when the test was created.
     * Read-only field set by the server.
     * Used for auditing and display purposes.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the test was last updated.
     * Read-only field set by the server.
     * Used for auditing and tracking changes.
     */
    private LocalDateTime updatedAt;
}