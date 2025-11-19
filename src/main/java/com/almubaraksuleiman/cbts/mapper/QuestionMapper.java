//package com.almubaraksuleiman.cbts.mapper;
//
//import com.almubaraksuleiman.cbts.dto.QuestionDto;
//import com.almubaraksuleiman.cbts.examiner.model.*;
//import org.springframework.stereotype.Component;
//
///**
// * @author Almubarak Suleiman
// * @version 1.0
// * @since 2025
// **/
//
//@Component
//public class QuestionMapper {
//
//    public QuestionDto toDto(Question question) {
//        if (question == null) return null;
//
//        QuestionDto.QuestionDtoBuilder dto = QuestionDto.builder()
//                .id(question.getId())
//                .text(question.getText())
//                .choices(question.getChoices())
//                .correctAnswer(question.getCorrectAnswer())
//                .type(question.getType())
//                .maxMarks(question.getMaxMarks());
//
//
//
//
//        return dto.build();
//    }
//
//    public Question toEntity(QuestionDto dto) {
//        if (dto == null) return null;
//
//        Question.QuestionBuilder entity = Question.builder()
//                .id(dto.getId())
//                .text(dto.getText())
//                .choices(dto.getChoices())
//                .correctAnswer(dto.getCorrectAnswer())
//                .type(dto.getType())
//                .maxMarks(dto.getMaxMarks());
//
//        return entity.build();
//    }
//}















// Package declaration - organizes classes within the mapper package
package com.almubaraksuleiman.cbts.mapper;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.dto.QuestionDto;
import com.almubaraksuleiman.cbts.examiner.model.Question;
import org.springframework.stereotype.Component;

/**
 * Enhanced QuestionMapper with comprehensive field mapping and media support.
 *
 * This mapper handles bidirectional conversion between Question entity and QuestionDto.
 * It implements the Data Transfer Object pattern to separate persistence concerns
 * from API contract and business logic.
 *
 * The mapper ensures:
 * - Proper conversion of all fields including media and advanced configurations
 * - Handling of null values and edge cases
 * - Consistent data transformation between layers
 * - Support for file uploads and media management
 *
 * @author Almubarak Suleiman
 * @version 2.0
 * @since 2025
 */
@Component
public class QuestionMapper {

    /**
     * Converts a Question entity to a QuestionDto for API responses.
     *
     * This method transforms the persistent entity into a data transfer object
     * suitable for client consumption. It includes all persistent fields plus
     * calculated fields and media information.
     *
     * @param question The Question entity to convert, can be null
     * @return QuestionDto The converted DTO, or null if input is null
     *
     * Conversion Process:
     * 1. Maps all persistent fields from entity to DTO
     * 2. Includes media attachment information
     * 3. Maps advanced question configurations
     * 4. Includes audit timestamps for tracking
     */
    public QuestionDto toDto(Question question) {
        if (question == null) {
            return null;
        }

        return QuestionDto.builder()
                .id(question.getId())
                .text(question.getText())
                .choices(question.getChoices())
                .correctAnswer(question.getCorrectAnswer())
                .type(question.getType())
                .maxMarks(question.getMaxMarks())
                .mediaType(question.getMediaType())
                .mediaPath(question.getMediaPath())
                .mediaCaption(question.getMediaCaption())
                .questionOrder(question.getQuestionOrder())
                .difficulty(question.getDifficulty())
                .explanation(question.getExplanation())
                .category(question.getCategory())
                .timeLimitSeconds(question.getTimeLimitSeconds())
                .allowPartialCredit(question.getAllowPartialCredit())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    /**
     * Converts a QuestionDto to a Question entity for persistence.
     *
     * This method transforms a data transfer object from the client into a
     * persistent entity for database operations. It handles all fields including
     * media configurations and advanced settings.
     *
     * Note: Transient fields (mediaFileData, mediaFileName, mediaFileSize) are not mapped
     * as they are used only during upload operations.
     *
     * @param dto The QuestionDto to convert, can be null
     * @return Question The converted entity, or null if input is null
     *
     * Conversion Process:
     * 1. Maps all DTO fields to entity using builder pattern
     * 2. Excludes transient fields used for file uploads
     * 3. Uses Lombok builder for safe and readable object construction
     * 4. Handles null values gracefully through builder pattern
     */
    public Question toEntity(QuestionDto dto) {
        if (dto == null) {
            return null;
        }

        return Question.builder()
                .id(dto.getId())
                .text(dto.getText())
                .choices(dto.getChoices())
                .correctAnswer(dto.getCorrectAnswer())
                .type(dto.getType())
                .maxMarks(dto.getMaxMarks())
                .mediaType(dto.getMediaType())
                .mediaPath(dto.getMediaPath())
                .mediaCaption(dto.getMediaCaption())
                .questionOrder(dto.getQuestionOrder())
                .difficulty(dto.getDifficulty())
                .explanation(dto.getExplanation())
                .category(dto.getCategory())
                .timeLimitSeconds(dto.getTimeLimitSeconds())
                .allowPartialCredit(dto.getAllowPartialCredit())
                .build();
    }

    /**
     * Updates an existing Question entity with values from a QuestionDto.
     *
     * This method is useful for partial updates where you don't want to
     * replace the entire entity but only update provided fields.
     *
     * @param question The existing Question entity to update
     * @param dto The QuestionDto containing updated values
     *
     * Update Logic:
     * - Only updates non-null fields from DTO
     * - Preserves existing values for null DTO fields
     * - Handles media updates appropriately
     */
    public void updateEntityFromDto(Question question, QuestionDto dto) {
        if (question == null || dto == null) {
            return;
        }

        // Update basic fields if provided in DTO
        if (dto.getText() != null) {
            question.setText(dto.getText());
        }
        if (dto.getChoices() != null) {
            question.setChoices(dto.getChoices());
        }
        if (dto.getCorrectAnswer() != null) {
            question.setCorrectAnswer(dto.getCorrectAnswer());
        }
        if (dto.getType() != null) {
            question.setType(dto.getType());
        }
        if (dto.getMaxMarks() != null) {
            question.setMaxMarks(dto.getMaxMarks());
        }

        // Update media fields if provided
        if (dto.getMediaType() != null) {
            question.setMediaType(dto.getMediaType());
        }
        if (dto.getMediaPath() != null) {
            question.setMediaPath(dto.getMediaPath());
        }
        if (dto.getMediaCaption() != null) {
            question.setMediaCaption(dto.getMediaCaption());
        }

        // Update advanced fields if provided
        if (dto.getQuestionOrder() != null) {
            question.setQuestionOrder(dto.getQuestionOrder());
        }
        if (dto.getDifficulty() != null) {
            question.setDifficulty(dto.getDifficulty());
        }
        if (dto.getExplanation() != null) {
            question.setExplanation(dto.getExplanation());
        }
        if (dto.getCategory() != null) {
            question.setCategory(dto.getCategory());
        }
        if (dto.getTimeLimitSeconds() != null) {
            question.setTimeLimitSeconds(dto.getTimeLimitSeconds());
        }
        if (dto.getAllowPartialCredit() != null) {
            question.setAllowPartialCredit(dto.getAllowPartialCredit());
        }
    }
}