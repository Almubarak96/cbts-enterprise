//package com.almubaraksuleiman.cbts.dto;
//
//import com.almubaraksuleiman.cbts.examiner.model.MediaType;
//import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.List;
//
///**
// * @author Almubarak Suleiman
// * @version 1.0
// * @since 2025
// **/
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class QuestionDto {
//    private Long studentExamQuestionId;  // Required for saving answers
//    private Long id;             // Optional for reference
//    private String text;
//    private String choices;
//    private String correctAnswer;
//    private QuestionType type;
//    private Double maxMarks;
//    private String savedAnswer;          // Current answer if already saved
//
//
//
//    private MediaType mediaType;
//    private String mediaPath;
//    private String mediaCaption;
//    private Integer questionOrder;
//
//
//
//}













// Package declaration - organizes classes within the DTO package
package com.almubaraksuleiman.cbts.dto;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.examiner.model.DifficultyLevel;
import com.almubaraksuleiman.cbts.examiner.model.MediaType;
import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced Question Data Transfer Object (DTO) with comprehensive question management.
 *
 * This DTO represents the question data transferred between client and server layers.
 * It includes all question properties including media attachments, difficulty levels,
 * explanations, and advanced question configurations.
 *
 * The DTO pattern is used to:
 * - Decouple the internal entity structure from external API
 * - Control which fields are exposed to clients
 * - Provide calculated fields that aren't persisted
 * - Handle file uploads and media management
 *
 * @author Almubarak Suleiman
 * @version 2.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionDto {

    /**
     * Unique identifier for the question.
     * Used for update operations and question references.
     */
    private Long id;

    /**
     * The actual question text or prompt.
     * Required field for all question types.
     * Supports HTML formatting for rich content.
     */
    private String text;

    /**
     * Available answer choices in string format.
     * Format depends on question type:
     * - Multiple Choice: comma-separated or JSON array
     * - True/False: "True,False"
     * - Matching: JSON with left-right pairs
     */
    private String choices;

    /**
     * Correct answer for the question.
     * Format varies by question type.
     */
    private String correctAnswer;

    /**
     * Type of question determining presentation and evaluation.
     * Controls form behavior and validation rules.
     */
    private QuestionType type;

    /**
     * Maximum marks awarded for correct answer.
     * Used in score calculation and grading.
     */
    private Double maxMarks;

    /**
     * Student's saved answer during test taking.
     * Used for resume functionality and answer persistence.
     */
    private String savedAnswer;

    /**
     * Reference to student exam question for answer tracking.
     */
    private Long studentExamQuestionId;

    /**
     * Type of media attached to the question.
     * Controls media preview and validation.
     */
    private MediaType mediaType;

    /**
     * Media file path, URL, or base64 data.
     * Supports multiple storage backends (local, AWS, Azure, GCP).
     */
    private String mediaPath;

    /**
     * Media caption for accessibility and context.
     * Provides alternative text for media content.
     */
    private String mediaCaption;

    /**
     * Order/sequence of question within test.
     * Controls presentation order to students.
     */
    private Integer questionOrder;

    /**
     * Difficulty level for adaptive testing.
     * Used in question selection algorithms.
     */
    private DifficultyLevel difficulty;

    /**
     * Explanation of correct answer for review.
     * Displayed to students after test completion.
     */
    private String explanation;

    /**
     * Category/topic for organization.
     * Enables filtering and categorization.
     */
    private String category;

    /**
     * Time limit for this specific question.
     * Overrides test duration if specified.
     */
    private Integer timeLimitSeconds;

    /**
     * Flag for partial credit allowance.
     * Enables partial scoring for complex questions.
     */
    private Boolean allowPartialCredit;

    /**
     * File data for media upload (transient).
     * Used during file upload operations.
     */
    private transient String mediaFileData;

    /**
     * File name for media upload (transient).
     * Preserves original filename during upload.
     */
    private transient String mediaFileName;

    /**
     * File size for validation (transient).
     * Ensures uploads meet size requirements.
     */
    private transient Long mediaFileSize;

    // Audit Fields (Read-only)

    /**
     * Timestamp when question was created.
     * Read-only field set by server.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Timestamp when question was last updated.
     * Read-only field set by server.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Helper Methods

    /**
     * Checks if question has attached media.
     *
     * @return boolean True if media is attached
     */
    public boolean hasMedia() {
        return mediaType != null && mediaType != MediaType.NONE &&
                mediaPath != null && !mediaPath.trim().isEmpty();
    }

    /**
     * Validates question configuration consistency.
     *
     * @return boolean True if question configuration is valid
     */
    public boolean isValidConfiguration() {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        if (type != null) {
            switch (type) {
                case MULTIPLE_CHOICE:
                case MULTIPLE_SELECT:
                    return choices != null && !choices.trim().isEmpty() &&
                            correctAnswer != null && !correctAnswer.trim().isEmpty();
                case TRUE_FALSE:
                    return "True".equals(correctAnswer) || "False".equals(correctAnswer);
                case FILL_IN_THE_BLANK:
                case ESSAY:
                    return correctAnswer != null && !correctAnswer.trim().isEmpty();
                case MATCHING:
                    return choices != null && !choices.trim().isEmpty();
                default:
                    return true;
            }
        }
        return false;
    }
}