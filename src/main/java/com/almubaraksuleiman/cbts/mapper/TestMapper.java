// Package declaration - organizes classes within the mapper package
package com.almubaraksuleiman.cbts.mapper;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.dto.TestDto;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import org.springframework.stereotype.Component;

/**
 * Enhanced TestMapper with comprehensive test window support.
 *
 * This mapper handles bidirectional conversion between Test entity and TestDto.
 * It implements the Data Transfer Object pattern to separate persistence concerns
 * from API contract and business logic.
 *
 * The mapper ensures:
 * - Proper conversion of all fields including new test window configurations
 * - Handling of null values and edge cases
 * - Calculation of transient fields for DTO
 * - Consistent data transformation between layers
 *
 * @author Almubarak Suleiman
 * @version 2.0
 * @since 2025
 */
@Component // Marks this class as a Spring component for dependency injection
public class TestMapper {

    /**
     * Converts a Test entity to a TestDto for API responses.
     *
     * This method transforms the persistent entity into a data transfer object
     * suitable for client consumption. It includes all persistent fields plus
     * calculated fields like currentStatus and currentlyAccessible.
     *
     * @param test The Test entity to convert, can be null
     * @return TestDto The converted DTO, or null if input is null
     *
     * Conversion Process:
     * 1. Maps all persistent fields from entity to DTO
     * 2. Includes new test window configuration fields
     * 3. Calculates and sets transient status fields
     * 4. Includes audit timestamps for tracking
     *
     * Example Usage:
     * {@code
     * Test test = testRepository.findById(1L).orElse(null);
     * TestDto testDto = testMapper.toDto(test);
     * }
     */
    public TestDto toDto(Test test) {
        // Early return for null input
        if (test == null) {
            return null;
        }

        // Create new DTO instance
        TestDto testDto = new TestDto();

        // Map existing basic fields
        testDto.setId(test.getId());
        testDto.setTitle(test.getTitle());
        testDto.setNumberOfQuestions(test.getNumberOfQuestions());
        testDto.setRandomizeQuestions(test.getRandomizeQuestions());
        testDto.setShuffleChoices(test.getShuffleChoices());
        testDto.setDescription(test.getDescription());
        testDto.setDurationMinutes(test.getDurationMinutes());
        testDto.setTotalMarks(test.getTotalMarks());
        testDto.setPublished(test.getPublished());
        testDto.setPassingScore(test.getPassingScore());

        // Map new test window configuration fields
        testDto.setScheduledStartTime(test.getScheduledStartTime());
        testDto.setScheduledEndTime(test.getScheduledEndTime());
        testDto.setTimeEnforcement(test.getTimeEnforcement());
        testDto.setMaxAttempts(test.getMaxAttempts());
        testDto.setStartBufferMinutes(test.getStartBufferMinutes());
        testDto.setEndBufferMinutes(test.getEndBufferMinutes());
        testDto.setAllowedIPs(test.getAllowedIPs());
        testDto.setSecureBrowser(test.getSecureBrowser());

        // Map calculated fields (transient in entity, persistent in DTO for client)
        testDto.setCurrentStatus(test.getCurrentStatus());
        testDto.setCurrentlyAccessible(test.isCurrentlyAccessible());

        // Map audit fields
        testDto.setCreatedAt(test.getCreatedAt());
        testDto.setUpdatedAt(test.getUpdatedAt());

        return testDto;
    }

    /**
     * Converts a TestDto to a Test entity for persistence.
     *
     * This method transforms a data transfer object from the client into a
     * persistent entity for database operations. It handles all fields including
     * the new test window configurations.
     *
     * Note: Calculated fields (currentStatus, currentlyAccessible) are not mapped
     * as they are computed by the entity based on other persistent fields.
     *
     * @param testDto The TestDto to convert, can be null
     * @return Test The converted entity, or null if input is null
     *
     * Conversion Process:
     * 1. Maps all DTO fields to entity using builder pattern
     * 2. Excludes calculated fields that are computed by entity
     * 3. Uses Lombok builder for safe and readable object construction
     * 4. Handles null values gracefully through builder pattern
     *
     * Example Usage:
     * {@code
     * TestDto testDto = new TestDto();
     * // ... set properties on testDto
     * Test test = testMapper.toEntity(testDto);
     * Test savedTest = testRepository.save(test);
     * }
     */
    public Test toEntity(TestDto testDto) {
        // Early return for null input
        if (testDto == null) {
            return null;
        }

        // Use Lombok builder for safe and readable entity construction
        Test.TestBuilder test = Test.builder();

        // Map existing basic fields
        test.id(testDto.getId());
        test.title(testDto.getTitle());
        test.numberOfQuestions(testDto.getNumberOfQuestions());
        test.randomizeQuestions(testDto.getRandomizeQuestions());
        test.shuffleChoices(testDto.getShuffleChoices());
        test.description(testDto.getDescription());
        test.durationMinutes(testDto.getDurationMinutes());
        test.totalMarks(testDto.getTotalMarks());
        test.published(testDto.getPublished());
        test.passingScore(testDto.getPassingScore());

        // Map new test window configuration fields
        test.scheduledStartTime(testDto.getScheduledStartTime());
        test.scheduledEndTime(testDto.getScheduledEndTime());
        test.timeEnforcement(testDto.getTimeEnforcement());
        test.maxAttempts(testDto.getMaxAttempts());
        test.startBufferMinutes(testDto.getStartBufferMinutes());
        test.endBufferMinutes(testDto.getEndBufferMinutes());
        test.allowedIPs(testDto.getAllowedIPs());
        test.secureBrowser(testDto.getSecureBrowser());

        // Note: Calculated fields (currentStatus, currentlyAccessible) are not mapped
        // as they are computed by the entity based on other fields

        // Build and return the entity
        return test.build();
    }

    /**
     * Updates an existing Test entity with values from a TestDto.
     *
     * This method is useful for partial updates where you don't want to
     * replace the entire entity but only update provided fields.
     *
     * @param test The existing Test entity to update
     * @param testDto The TestDto containing updated values
     *
     * Example Usage:
     * {@code
     * Test existingTest = testRepository.findById(1L).orElseThrow();
     * testMapper.updateEntityFromDto(existingTest, testDto);
     * testRepository.save(existingTest);
     * }
     */
    public void updateEntityFromDto(Test test, TestDto testDto) {
        if (test == null || testDto == null) {
            return;
        }

        // Update basic fields if provided in DTO
        if (testDto.getTitle() != null) {
            test.setTitle(testDto.getTitle());
        }
        if (testDto.getDescription() != null) {
            test.setDescription(testDto.getDescription());
        }
        if (testDto.getNumberOfQuestions() != null) {
            test.setNumberOfQuestions(testDto.getNumberOfQuestions());
        }
        if (testDto.getDurationMinutes() != null) {
            test.setDurationMinutes(testDto.getDurationMinutes());
        }
        if (testDto.getTotalMarks() != null) {
            test.setTotalMarks(testDto.getTotalMarks());
        }
        if (testDto.getPassingScore() != null) {
            test.setPassingScore(testDto.getPassingScore());
        }
        if (testDto.getPublished() != null) {
            test.setPublished(testDto.getPublished());
        }
        if (testDto.getRandomizeQuestions() != null) {
            test.setRandomizeQuestions(testDto.getRandomizeQuestions());
        }
        if (testDto.getShuffleChoices() != null) {
            test.setShuffleChoices(testDto.getShuffleChoices());
        }

        // Update test window configuration fields if provided
        if (testDto.getScheduledStartTime() != null) {
            test.setScheduledStartTime(testDto.getScheduledStartTime());
        }
        if (testDto.getScheduledEndTime() != null) {
            test.setScheduledEndTime(testDto.getScheduledEndTime());
        }
        if (testDto.getTimeEnforcement() != null) {
            test.setTimeEnforcement(testDto.getTimeEnforcement());
        }
        if (testDto.getMaxAttempts() != null) {
            test.setMaxAttempts(testDto.getMaxAttempts());
        }
        if (testDto.getStartBufferMinutes() != null) {
            test.setStartBufferMinutes(testDto.getStartBufferMinutes());
        }
        if (testDto.getEndBufferMinutes() != null) {
            test.setEndBufferMinutes(testDto.getEndBufferMinutes());
        }
        if (testDto.getAllowedIPs() != null) {
            test.setAllowedIPs(testDto.getAllowedIPs());
        }
        if (testDto.getSecureBrowser() != null) {
            test.setSecureBrowser(testDto.getSecureBrowser());
        }
    }
}