// Package declaration - organizes classes within the repository package
package com.almubaraksuleiman.cbts.examiner.repository;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.examiner.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Question entity operations.
 * Extends JpaRepository to inherit common CRUD operations and provides
 * custom query methods for question-related data access.

 * This interface handles database operations for questions, including
 * finding questions by test ID with support for pagination.

 * @ Repository annotation is optional here since JpaRepository is already
 * annotated, but it makes the Spring component scanning more explicit.

 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Repository // Explicitly marks this as a Spring repository component
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Finds all questions belonging to a specific test.
     * Spring Data JPA automatically generates the query based on method name.
     * The naming convention "findBy[Property][Condition]" creates the appropriate SQL.
     *
     * @param testId The ID of the test to find questions for
     * @return List<Question> All questions associated with the given test ID

     * Generated SQL equivalent:
     * SELECT * FROM questions WHERE test_id = :testId
     */
    List<Question> findByTestId(Long testId);

    /**
     * Finds paginated questions belonging to a specific test.
     * Supports pagination and sorting for large datasets.
     *
     * @param sessionId The ID of the test/session to find questions for
     * @param pageable Pagination information (page number, size, sorting)
     * @return Page<Question> Paginated result containing questions and pagination metadata

     * Generated SQL equivalent with pagination:
     * SELECT * FROM questions WHERE test_id = :sessionId
     * LIMIT :pageSize OFFSET :pageNumber

     * The Page object contains:
     * - List of questions for the current page
     * - Total number of questions
     * - Total number of pages
     * - Current page number
     * - Page size
     */
    //Page<Question> findByTestId(Long sessionId, Pageable pageable);


    /*
     * Inherited methods from JpaRepository include:
     * - Question save(Question question) - Save a question
     * - Optional<Question> findById(Long id) - Find question by ID
     * - List<Question> findAll() - Find all questions
     * - void deleteById(Long id) - Delete question by ID
     * - boolean existsById(Long id) - Check if question exists
     * - long count() - Count all questions
     * - Page<Question> findAll(Pageable pageable) - Paginated findAll
     */

    /*
     * Potential additional custom methods that could be added:
     *
     * // Find questions by type
     * List<Question> findByType(QuestionType type);

     * // Find questions by test ID and type
     * List<Question> findByTestIdAndType(Long testId, QuestionType type);

     * // Count questions in a test
     * long countByTestId(Long testId);

     * // Find questions with maximum marks greater than specified value
     * List<Question> findByTestIdAndMaxMarksGreaterThan(Long testId, Double minMarks);
     *
     * // Find questions containing specific text (case-insensitive)
     * List<Question> findByTestIdAndTextContainingIgnoreCase(Long testId, String searchText);
     */




    // NEW: Paginated version of findByTestId
    Page<Question> findByTestId(Long testId, Pageable pageable);

    // Search questions by text, choices, or correct answer within a test
    Page<Question> findByTestIdAndTextContainingIgnoreCaseOrChoicesContainingIgnoreCaseOrCorrectAnswerContainingIgnoreCase(
            Long testId, String text, String choices, String correctAnswer, Pageable pageable);

    // Filter by question type within a test
    Page<Question> findByTestIdAndType(Long testId, String type, Pageable pageable);

    // Filter by marks range within a test
    Page<Question> findByTestIdAndMaxMarksBetween(Long testId, Double minMarks, Double maxMarks, Pageable pageable);

    // Advanced search with multiple criteria within a test
    @Query("SELECT q FROM Question q WHERE q.test.id = :testId AND " +
            "(:keyword IS NULL OR LOWER(q.text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(q.choices) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(q.correctAnswer) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:type IS NULL OR q.type = :type) AND " +
            "(:minMarks IS NULL OR q.maxMarks >= :minMarks) AND " +
            "(:maxMarks IS NULL OR q.maxMarks <= :maxMarks)")
    Page<Question> findByAdvancedSearch(
            @Param("testId") Long testId,
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("minMarks") Double minMarks,
            @Param("maxMarks") Double maxMarks,
            Pageable pageable);


}