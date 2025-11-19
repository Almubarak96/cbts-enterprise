package com.almubaraksuleiman.cbts.student.service;

import com.almubaraksuleiman.cbts.dto.QuestionDto;
import com.almubaraksuleiman.cbts.dto.StudentAnswerDto;
import com.almubaraksuleiman.cbts.dto.ExamSessionDto;
import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StudentExamService {

    /**
     * Starts a new exam session for a student and specified test.
     */
    ExamSessionDto startExam(Long studentId, Long testId);

    /**
     * Retrieves paginated questions for an exam session.
     * Uses studentId and testId to identify the correct session
     */
    Page<QuestionDto> getQuestions(Long studentId, Long testId, int page, int size);

    /**
     * Retrieves all question IDs for an exam session.
     * FIXED: Uses studentId and testId to identify the correct session
     */
    List<Long> getQuestionIds(Long studentId, Long testId);

    /**
     * Saves student answers for questions in an exam session.
     * Uses studentId and testId to identify the correct session
     */
    void saveAnswers(Long studentId, Long testId, List<StudentAnswerDto> answers);

    /**
     * Marks an exam session as completed and triggers the grading process.
     * Uses studentId and testId to identify the correct session
     */
    void completeExam(Long studentId, Long testId);

    /**
     * Retrieves questions filtered by type for an exam session.
     * Uses studentId and testId to identify the correct session
     */
    Page<QuestionDto> getQuestionsByType(Long studentId, Long testId, QuestionType type, int page, int size);
}