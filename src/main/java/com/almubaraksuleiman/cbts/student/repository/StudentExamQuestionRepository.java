
package com.almubaraksuleiman.cbts.student.repository;

import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import com.almubaraksuleiman.cbts.student.model.StudentExamQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StudentExamQuestionRepository extends JpaRepository<StudentExamQuestion, Long> {

    /**
     * Finds all question IDs assigned to a specific exam session.
     */
    @Query("SELECT seq.id FROM StudentExamQuestion seq WHERE seq.studentExam.sessionId = :sessionId")
    List<Long> findQuestionIdsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Finds paginated question assignments for a specific exam session.
     */
    Page<StudentExamQuestion> findByStudentExam_SessionId(Long sessionId, Pageable pageable);

    /**
     * Ordered pagination for questions - uses the stored question order
     */
    @Query("SELECT seq FROM StudentExamQuestion seq WHERE seq.studentExam.sessionId = :sessionId ORDER BY seq.order")
    Page<StudentExamQuestion> findByStudentExam_SessionIdOrdered(@Param("sessionId") Long sessionId, Pageable pageable);

    /**
     * Counts the number of question assignments for a specific exam session.
     */
    Number countByStudentExam_SessionId(Long sessionId);

    /**
     * Retrieves a map of question IDs and their answered status for a session.
     */
    @Query("SELECT new map(seq.id as id, seq.answered as answered) " +
            "FROM StudentExamQuestion seq WHERE seq.studentExam.sessionId = :sessionId")
    List<Map<String, Object>> findQuestionIdsAndAnswered(@Param("sessionId") Long sessionId);

    /**
     * Finds questions by session ID and question type with pagination.
     */
    Page<StudentExamQuestion> findByStudentExam_SessionIdAndQuestion_Type(
            Long sessionId,
            QuestionType type,
            Pageable pageable
    );

    /**
     * Ordered pagination for questions by type - uses the stored question order
     */
    @Query("SELECT seq FROM StudentExamQuestion seq WHERE seq.studentExam.sessionId = :sessionId AND seq.question.type = :type ORDER BY seq.order")
    Page<StudentExamQuestion> findByStudentExam_SessionIdAndQuestion_TypeOrdered(
            @Param("sessionId") Long sessionId,
            @Param("type") QuestionType type,
            Pageable pageable
    );

    /**
     * Order by the stored question order to match exam sequence
     */
    @Query("""
        SELECT new map(seq.id as questionId, seq.answered as answered, q.type as type, 
                      q.id as originalQuestionId, seq.studentExam.sessionId as sessionId, seq.order as displayOrder)
        FROM StudentExamQuestion seq 
        JOIN seq.question q 
        JOIN seq.studentExam se 
        WHERE se.studentId = :studentId AND se.test.id = :testId
        ORDER BY seq.order
    """)
    List<Map<String, Object>> findQuestionIdsAnsweredAndType(
            @Param("studentId") Long studentId,
            @Param("testId") Long testId
    );

    /**
     * Alternative method to find by session ID (for backward compatibility)
     */
    @Query("""
        SELECT new map(seq.id as questionId, seq.answered as answered, q.type as type, 
                      q.id as originalQuestionId)
        FROM StudentExamQuestion seq 
        JOIN seq.question q 
        WHERE seq.studentExam.sessionId = :sessionId
        ORDER BY q.id
    """)
    List<Map<String, Object>> findQuestionIdsAnsweredAndTypeBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Find the session ID for a student and test combination
     * Useful for getting the correct session when switching between tests
     */
    @Query("SELECT se.sessionId FROM StudentExam se WHERE se.studentId = :studentId AND se.test.id = :testId")
    Long findSessionIdByStudentIdAndTestId(@Param("studentId") Long studentId, @Param("testId") Long testId);

    /**
     * Check if a student has an active session for a specific test
     */
    @Query("SELECT COUNT(se) > 0 FROM StudentExam se WHERE se.studentId = :studentId AND se.test.id = :testId AND se.completed = false")
    boolean hasActiveSession(@Param("studentId") Long studentId, @Param("testId") Long testId);
}