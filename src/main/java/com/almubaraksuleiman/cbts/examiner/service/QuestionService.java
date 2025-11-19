// Package declaration - organizes classes within the service package
package com.almubaraksuleiman.cbts.examiner.service;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.dto.QuestionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Question management operations.
 * Defines the contract for business logic related to question creation, retrieval,
 * modification, deletion, and bulk operations within tests.

 * This interface follows the Service Layer pattern, separating business logic
 * from controller and repository layers for question-related operations.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
public interface QuestionService {

    /**
     * Adds a new question to a specific test.
     * Validates the test exists, converts DTO to entity, and establishes the relationship.
     *
     * @param testId The ID of the test to which the question will be added
     * @param dto Data Transfer Object containing question information
     * @return QuestionDto The created question with generated ID and persisted data
     * @throws IllegalArgumentException if test with given ID is not found
     * @throws IllegalArgumentException if question data validation fails
     */
    QuestionDto addQuestionToTest(Long testId, QuestionDto dto);

    /**
     * Retrieves all questions belonging to a specific test.
     * Returns questions converted to DTOs for the given test ID.
     *
     * @param testId The ID of the test whose questions to retrieve
     * @return List<QuestionDto> List of all questions associated with the test
     *         Returns empty list if no questions exist for the test
     * @throws IllegalArgumentException if test with given ID is not found
     */
    List<QuestionDto> getQuestionsByTest(Long testId);

    /**
     * Bulk uploads questions to a specific test from a file (CSV or Excel format).
     * Processes multiple question records from a structured file and persists them
     * to the specified test. Supports both CSV and Excel file formats.
     *
     * @param testId The ID of the test to which questions will be added
     * @param file The multipart file containing question data in supported format
     * @throws IllegalArgumentException if test with given ID is not found
     * @throws IllegalArgumentException if file is empty, invalid, or unsupported format
     * @throws RuntimeException if file processing, parsing, or data persistence fails
     */
    void bulkUploadQuestions(Long testId, MultipartFile file);

    @Transactional
    QuestionDto updateQuestion(Long id, QuestionDto questionDto, MultipartFile mediaFile);

    /**
     * Retrieves a specific question by its unique identifier.
     *
     * @param questionId The unique ID of the question to retrieve
     * @return QuestionDto The question data transfer object
     * @throws IllegalArgumentException if no question exists with the given ID
     */
    QuestionDto getQuestionById(Long questionId);

    @Transactional
    QuestionDto addQuestionToTest(Long testId, QuestionDto questionDto, MultipartFile mediaFile);

    /**
     * Updates an existing question with new data.
     * Finds the question by ID, applies updates from the DTO, and persists changes.
     * Note: The test association typically remains unchanged.
     *
     * @param questionId The unique ID of the question to update
     * @param questionDto Data Transfer Object containing updated question information
     * @return QuestionDto The updated question data
     * @throws IllegalArgumentException if no question exists with the given ID
     * @throws IllegalArgumentException if question data validation fails
     */
    QuestionDto updateQuestion(Long questionId, QuestionDto questionDto);

    /**
     * Deletes a question by its unique identifier.
     * Removes the question from the database and potentially from any test associations.
     *
     * @param questionId The unique ID of the question to delete
     * @throws IllegalArgumentException if no question exists with the given ID
     */
    void deleteQuestion(Long questionId);

    /*
     * Potential additional methods that could be added:

     * // Get questions by type within a test
     * List<QuestionDto> getQuestionsByTestAndType(Long testId, QuestionType type);

     * // Search questions containing specific text within a test
     * List<QuestionDto> searchQuestionsInTest(Long testId, String searchText);

     * // Get paginated questions for a test
     * Page<QuestionDto> getQuestionsByTest(Long testId, Pageable pageable);

     * // Count questions in a test
     * long countQuestionsByTest(Long testId);

     * // Validate question data before save/update
     * boolean validateQuestion(QuestionDto questionDto);

     * // Duplicate a question within or across tests
     * QuestionDto duplicateQuestion(Long questionId, Long targetTestId);

     * // Reorder questions within a test
     * void reorderQuestions(Long testId, List<Long> questionIdsInOrder);
     */






    Page<QuestionDto> getQuestionsByTest(Long testId, Pageable pageable);
    Page<QuestionDto> searchQuestions(Long testId, String keyword, Pageable pageable);
    Page<QuestionDto> getQuestionsByType(Long testId, String type, Pageable pageable);
    Page<QuestionDto> getQuestionsByMarksRange(Long testId, Double minMarks, Double maxMarks, Pageable pageable);
    Page<QuestionDto> findByAdvancedSearch(Long testId, String keyword, String type, Double minMarks, Double maxMarks, Pageable pageable);
}