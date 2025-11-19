// Package declaration - organizes classes within the repository package
package com.almubaraksuleiman.cbts.examiner.repository;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.dto.TestDto;
import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.examiner.model.Question;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for Test entity operations.
 * Extends JpaRepository to inherit common CRUD operations and query methods.

 * This interface provides data access methods for Test entities without requiring
 * explicit implementation - Spring Data JPA generates the implementation automatically.

 * Key Features:
 * - Automatic CRUD operations (save, findById, findAll, delete, etc.)
 * - Pagination and sorting support
 * - Query method generation from method names
 * - Transaction management
 *
 * @param <Test> The entity type this repository manages
 * @param <Long> The type of the entity's ID field

 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

public interface TestRepository extends JpaRepository<Test, Long> {
    /*
     * Inherited methods from JpaRepository:

     * CRUD Operations:
     * - <S extends Test> S save(S entity) - Saves a test entity
     * - Optional<Test> findById(Long id) - Finds test by ID
     * - List<Test> findAll() - Retrieves all tests
     * - void deleteById(Long id) - Deletes test by ID
     * - void delete(Test entity) - Deletes specific test
     * - boolean existsById(Long id) - Checks if test exists
     * - long count() - Returns total number of tests

     * Pagination and Sorting:
     * - Page<Test> findAll(Pageable pageable) - Paginated results
     * - List<Test> findAll(Sort sort) - Sorted results

     * Batch Operations:
     * - <S extends Test> List<S> saveAll(Iterable<S> entities) - Saves multiple tests
     * - void deleteAll(Iterable<? extends Test> entities) - Deletes multiple tests
     * - void deleteAll() - Deletes all tests

     * Custom query methods can be added here by following Spring Data JPA naming conventions:
     * Examples:
     * - List<Test> findByTitle(String title)
     * - List<Test> findByPublishedTrue()
     * - List<Test> findByNumberOfQuestionsGreaterThan(int count)
     * - List<Test> findByTitleContainingIgnoreCase(String keyword)
     */

    // Spring Data JPA will automatically implement these methods based on naming convention:
    // Example custom method that could be added:
    // List<Test> findByPublished(Boolean published);

    // Example method for finding tests with minimum duration:
    // List<Test> findByDurationMinutesGreaterThanEqual(int minutes);

    // Example method for finding tests by title pattern:
    // List<Test> findByTitleLike(String titlePattern);



    // Updated methods with pagination

    // Advanced search with multiple criteria
    @Query("SELECT t FROM Test t WHERE " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:published IS NULL OR t.published = :published) AND " +
            "(:minDuration IS NULL OR t.durationMinutes >= :minDuration) AND " +
            "(:maxDuration IS NULL OR t.durationMinutes <= :maxDuration)")
    Page<Test> findByAdvancedSearch
    (
            @Param("keyword") String keyword,
            @Param("published") Boolean published,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration,
            Pageable pageable);


    Page<Test> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String keyword, String keyword1, Pageable pageable);

    Page<Test> findByPublished(Boolean published, Pageable pageable);

    Page<Test> findByDurationMinutesBetween(Integer minDuration, Integer maxDuration, Pageable pageable);

    // Add this method to your existing TestRepository
    @Query("SELECT q FROM Test t JOIN t.questions q WHERE t.id = :testId")
    List<Question> findQuestionsByTestId(@Param("testId") Long testId);

    Page<Test> findAll(Specification<Test> spec, Pageable pageable);



    Page<Test> findAll(Pageable pageable);

    Page<Test> findByCreatedBy(Examiner examiner, Pageable pageable);


















    // For examiners to get their tests

    // For access control checks
    boolean existsByIdAndCreatedBy(Long id, Examiner createdBy);

    // For examiners to search their tests
    Page<Test> findByCreatedByAndTitleContainingIgnoreCaseOrCreatedByAndDescriptionContainingIgnoreCase(
            Examiner createdBy1, String title, Examiner createdBy2, String description, Pageable pageable);

    // For examiners to filter their tests by status
    Page<Test> findByCreatedByAndPublished(Examiner createdBy, Boolean published, Pageable pageable);

    // For examiners to filter by duration range
    Page<Test> findByCreatedByAndDurationMinutesBetween(
            Examiner createdBy, Integer minDuration, Integer maxDuration, Pageable pageable);

    // Advanced search for examiners
    @Query("SELECT t FROM Test t WHERE " +
            "t.createdBy = :examiner AND " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:published IS NULL OR t.published = :published) AND " +
            "(:minDuration IS NULL OR t.durationMinutes >= :minDuration) AND " +
            "(:maxDuration IS NULL OR t.durationMinutes <= :maxDuration)")
    Page<Test> findByExaminerAndAdvancedSearch(
            @Param("examiner") Examiner examiner,
            @Param("keyword") String keyword,
            @Param("published") Boolean published,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration,
            Pageable pageable);


}