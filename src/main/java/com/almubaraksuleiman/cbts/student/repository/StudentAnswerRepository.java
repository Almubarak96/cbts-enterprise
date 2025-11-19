// Package declaration - organizes classes within the student repository package
package com.almubaraksuleiman.cbts.student.repository;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import com.almubaraksuleiman.cbts.student.model.StudentAnswer;
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.model.StudentExamQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for StudentAnswer entity operations.
 * Extends JpaRepository to inherit common CRUD operations and provides
 * custom query methods for student answer-related data access.

 * This interface handles database operations for storing and retrieving
 * student answers to exam questions, with methods designed for various
 * use cases including grading and answer retrieval.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Repository // Explicitly marks this as a Spring repository component
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    /**
     * Finds all answers for a specific exam session.
     * Retrieves all answer records associated with a particular exam session ID.
     * This is useful for reviewing all responses in an exam or for bulk operations.
     *
     * @param sessionId The ID of the exam session
     * @return List<StudentAnswer> All answer records for the specified session
     *         Returns empty list if no answers found for the session

     * Generated SQL equivalent:
     * SELECT * FROM student_answers
     * WHERE session_id = :sessionId
     */
    List<StudentAnswer> findByStudentExamQuestion_StudentExam_SessionId(Long sessionId);

    /**
     * Finds a specific answer by its associated exam question instance.
     * Useful for retrieving the answer for a particular question within an exam session.
     *
     * @param seq The StudentExamQuestion instance to search for
     * @return Optional<StudentAnswer> The answer record if found, empty otherwise

     * Generated SQL equivalent:
     * SELECT * FROM student_answers
     * WHERE student_exam_question_id = :seq.id
     */
    Optional<StudentAnswer> findByStudentExamQuestion(StudentExamQuestion seq);

    /**
     * Finds all answers for a specific student exam.
     * This method is specifically used in the ExamGradingService for processing
     * and grading all answers in an exam session.
     * Returns an array for potential performance reasons or specific grading requirements.
     *
     * @param exam The StudentExam instance to search for
     * @return StudentAnswer[] Array of answer records for the specified exam
     *         Returns empty array if no answers found for the exam

     * Generated SQL equivalent:
     * SELECT * FROM student_answers
     * WHERE session_id = :exam.sessionId
     */
    List<StudentAnswer> findByStudentExam(StudentExam exam);



    List<StudentAnswer> findByQuestionId(Long questionId);

    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.studentExam.test.id = :testId")
    List<StudentAnswer> findByTestId(@Param("testId") Long testId);

    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.question.id = :questionId AND sa.studentExam.completed = true")
    List<StudentAnswer> findCompletedAnswersByQuestionId(@Param("questionId") Long questionId);




    //methods for manual grading
    @Query("SELECT sa FROM StudentAnswer sa " +
            "WHERE sa.studentExam.sessionId = :sessionId " +
            "AND sa.question.id = :questionId")
    Optional<StudentAnswer> findBySessionIdAndQuestionId(@Param("sessionId") Long sessionId,
                                                         @Param("questionId") Long questionId);

    @Query("SELECT sa FROM StudentAnswer sa " +
            "WHERE sa.studentExam.sessionId = :sessionId " +
            "AND sa.question.type = :questionType")
    List<StudentAnswer> findBySessionIdAndQuestionType(@Param("sessionId") Long sessionId,
                                                       @Param("questionType") QuestionType questionType);

    @Query("SELECT sa FROM StudentAnswer sa " +
            "WHERE sa.studentExam.test.id = :testId " +
            "AND sa.question.type = com.almubaraksuleiman.cbts.examiner.model.QuestionType.ESSAY " +
            "AND (sa.score IS NULL OR sa.score = 0)")
    Page<StudentAnswer> findUngradedEssaysByTestId(@Param("testId") Long testId, Pageable pageable);

    @Query("SELECT sa FROM StudentAnswer sa " +
            "WHERE sa.studentExam.test.id = :testId " +
            "AND sa.question.type = com.almubaraksuleiman.cbts.examiner.model.QuestionType.ESSAY")
    Page<StudentAnswer> findAllEssaysByTestId(@Param("testId") Long testId, Pageable pageable);


    @Query("SELECT sa FROM StudentAnswer sa " +
            "WHERE sa.studentExam.test.id = :testId " +
            "AND sa.question.type = com.almubaraksuleiman.cbts.examiner.model.QuestionType.ESSAY")
    List<StudentAnswer> findAllEssaysByTestId(@Param("testId") Long testId);

    @Query("SELECT COUNT(sa) FROM StudentAnswer sa " +
            "WHERE sa.studentExam.test.id = :testId " +
            "AND sa.question.type = com.almubaraksuleiman.cbts.examiner.model.QuestionType.ESSAY " +
            "AND sa.score IS NOT NULL AND sa.score > 0")
    Long countGradedEssaysByTestId(@Param("testId") Long testId);

    @Query("SELECT COUNT(sa) FROM StudentAnswer sa " +
            "WHERE sa.studentExam.test.id = :testId " +
            "AND sa.question.type = com.almubaraksuleiman.cbts.examiner.model.QuestionType.ESSAY " +
            "AND (sa.score IS NULL OR sa.score = 0)")
    Long countUngradedEssaysByTestId(@Param("testId") Long testId);


    /*
     * Inherited methods from JpaRepository include:
     *
     * CRUD Operations:
     * - StudentAnswer save(StudentAnswer entity) - Save an answer record
     * - Optional<StudentAnswer> findById(Long id) - Find by ID
     * - List<StudentAnswer> findAll() - Retrieve all answer records
     * - void deleteById(Long id) - Delete by ID
     * - boolean existsById(Long id) - Check if exists
     * - long count() - Count all answer records
     *
     * Pagination and Sorting:
     * - Page<StudentAnswer> findAll(Pageable pageable) - Paginated results
     * - List<StudentAnswer> findAll(Sort sort) - Sorted results
     */

    /*
     * Potential additional custom methods that could be added:
     *
     * // Find answers by session ID and question ID
     * Optional<StudentAnswer> findByStudentExam_SessionIdAndQuestion_Id(Long sessionId, Long questionId);
     *
     * // Find answers that have been scored (graded)
     * List<StudentAnswer> findByStudentExamAndScoreIsNotNull(StudentExam exam);
     *
     * // Find answers with score greater than value
     * List<StudentAnswer> findByStudentExamAndScoreGreaterThan(StudentExam exam, Double minScore);
     *
     * // Count answers in an exam session
     * Long countByStudentExam(StudentExam exam);
     *
     * // Find answers by multiple exam questions
     * List<StudentAnswer> findByStudentExamQuestionIn(List<StudentExamQuestion> examQuestions);
     *
     * // Find answers with specific answer text (for research/analysis)
     * List<StudentAnswer> findByStudentExamAndAnswerContaining(StudentExam exam, String answerText);
     *
     * // Find answers that haven't been graded yet
     * List<StudentAnswer> findByStudentExamAndScoreIsNull(StudentExam exam);
     */

    /*
     * Example usage of custom JPQL queries (if needed):
     *
     * @Query("SELECT sa FROM StudentAnswer sa WHERE sa.studentExam.sessionId = :sessionId " +
     *        "AND sa.question.type = :questionType")
     * List<StudentAnswer> findBySessionIdAndQuestionType(
     *     @Param("sessionId") Long sessionId,
     *     @Param("questionType") QuestionType questionType);
     *
     * @Query("SELECT AVG(sa.score) FROM StudentAnswer sa WHERE sa.studentExam = :exam")
     * Double findAverageScoreByExam(@Param("exam") StudentExam exam);
     */
}