// Package declaration - organizes classes within the student repository package
package com.almubaraksuleiman.cbts.student.repository;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for StudentExam entity operations.
 * Extends JpaRepository to inherit common CRUD operations and provides
 * custom query methods for student exam-related data access.

 * This interface handles database operations for student exam sessions,
 * including finding exams by student ID and test combinations.

 * Spring Data JPA automatically generates the implementation based on
 * method naming conventions.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/


@Repository // Explicitly marks this as a Spring repository component
public interface StudentExamRepository extends JpaRepository<StudentExam, Long> {

    /**
     * Finds all exam sessions for a specific student.
     * Returns a list of all exam attempts by the given student.
     *
     * @param studentId The ID of the student whose exams to retrieve
     * @return List<StudentExam> All exam sessions for the specified student
     *         Returns empty list if no exams found for the student

     * Generated SQL equivalent:
     * SELECT * FROM student_exam WHERE student_id = :studentId
     */
    List<StudentExam> findByStudentId(Long studentId);

    /**
     * Finds a specific exam session for a student and test combination.
     * Useful for checking if a student has already attempted a particular test
     * or for retrieving a specific exam attempt.
     *
     * @param studentId The ID of the student
     * @param testId The ID of the test
     * @return Optional<StudentExam> The exam session if found, empty otherwise

     * Generated SQL equivalent:
     * SELECT * FROM student_exam
     * WHERE student_id = :studentId AND test_id = :testId
     */
    Optional<StudentExam> findByStudentIdAndTestId(Long studentId, Long testId);






    List<StudentExam> findByTestId(Long testId);

    @Query("SELECT se FROM StudentExam se WHERE se.test.id = :testId AND se.completed = true")
    List<StudentExam> findCompletedExamsByTestId(@Param("testId") Long testId);

    long countByTestId(Long testId);

    long countByTestIdAndCompletedTrue(Long testId);

    Page<StudentExam> findAll(Specification<StudentExam> spec, Pageable pageable);

    Optional<StudentExam> findByStudentIdAndCompletedFalse(Long studentId);
    /*
     * Inherited methods from JpaRepository include:
     *
     * CRUD Operations:
     * - StudentExam save(StudentExam entity) - Save a student exam session
     * - Optional<StudentExam> findById(Long id) - Find exam session by ID
     * - List<StudentExam> findAll() - Retrieve all exam sessions
     * - void deleteById(Long id) - Delete exam session by ID
     * - boolean existsById(Long id) - Check if exam session exists
     * - long count() - Count all exam sessions
     *
     * Pagination and Sorting:
     * - Page<StudentExam> findAll(Pageable pageable) - Paginated results
     * - List<StudentExam> findAll(Sort sort) - Sorted results
     */

    /*
     * Potential additional custom methods that could be added:
     *
     * // Find exams by student ID with pagination
     * Page<StudentExam> findByStudentId(Long studentId, Pageable pageable);
     *
     * // Find completed exams for a student
     * List<StudentExam> findByStudentIdAndCompletedTrue(Long studentId);
     *
     * // Find graded exams for a student
     * List<StudentExam> findByStudentIdAndGradedTrue(Long studentId);
     *
     * // Find exams by test ID (all students who took a specific test)
     * List<StudentExam> findByTestId(Long testId);
     *
     * // Find exams by status
     * List<StudentExam> findByStudentIdAndStatus(Long studentId, String status);
     *
     * // Find exams with score greater than specified value
     * List<StudentExam> findByStudentIdAndScoreGreaterThan(Long studentId, Integer minScore);
     *
     * // Count exams by student and completion status
     * long countByStudentIdAndCompleted(Long studentId, Boolean completed);
     *
     * // Find the most recent exam for a student
     * Optional<StudentExam> findTopByStudentIdOrderByStartTimeDesc(Long studentId);
     *
     * // Find exams within a date range
     * List<StudentExam> findByStudentIdAndStartTimeBetween(
     *     Long studentId, LocalDateTime start, LocalDateTime end);
     */

    /*
     * Example usage of custom query with @Query annotation (if needed):
     *
     * @Query("SELECT se FROM StudentExam se WHERE se.studentId = :studentId " +
     *        "AND se.completed = true AND se.score IS NOT NULL " +
     *        "ORDER BY se.endTime DESC")
     * List<StudentExam> findCompletedGradedExamsByStudent(@Param("studentId") Long studentId);
     *
     * @Query("SELECT AVG(se.score) FROM StudentExam se WHERE se.testId = :testId")
     * Double findAverageScoreByTest(@Param("testId") Long testId);
     */
}
