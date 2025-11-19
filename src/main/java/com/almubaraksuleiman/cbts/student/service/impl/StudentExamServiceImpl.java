package com.almubaraksuleiman.cbts.student.service.impl;

import com.almubaraksuleiman.cbts.dto.ExamSessionDto;
import com.almubaraksuleiman.cbts.dto.ExamSessionService;
import com.almubaraksuleiman.cbts.dto.QuestionDto;
import com.almubaraksuleiman.cbts.dto.StudentAnswerDto;
import com.almubaraksuleiman.cbts.examiner.model.Question;
import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.almubaraksuleiman.cbts.examiner.repository.QuestionRepository;
import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
import com.almubaraksuleiman.cbts.examiner.service.TestInstructionsService;
import com.almubaraksuleiman.cbts.examiner.service.UnifiedWebSocketService;
import com.almubaraksuleiman.cbts.examiner.service.impl.ExamGradingService;
import com.almubaraksuleiman.cbts.student.model.Student;
import com.almubaraksuleiman.cbts.student.model.StudentAnswer;
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.model.StudentExamQuestion;
import com.almubaraksuleiman.cbts.student.repository.StudentAnswerRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentExamQuestionRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import com.almubaraksuleiman.cbts.student.service.StudentExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * StudentExamService implementation with comprehensive exam session management
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StudentExamServiceImpl implements StudentExamService {

    // Repository dependencies
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final StudentAnswerRepository answerRepository;
    private final StudentExamRepository studentExamRepository;
    private final StudentExamQuestionRepository studentExamQuestionRepository;
    private final StudentRepository studentRepository;

    // Service dependencies
    private final UnifiedWebSocketService unifiedWebSocketService;
    private final ExamSessionService examSessionService;
    private final ExamGradingService examGradingService;
    private final TestInstructionsService instructionsService;
    private final StudentEnrolledTestsService enrolledTestsService;

    /**
     * Starts a new exam session or resumes an existing one
     *
     * @param studentId The ID of the student starting the exam
     * @param testId The ID of the test to be taken
     * @return ExamSessionDto containing session details
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public ExamSessionDto startExam(Long studentId, Long testId) {
        log.info("Starting exam for student ID: {}, test ID: {}", studentId, testId);

        // Validate inputs
        validateStudentAndTest(studentId, testId);
        validateStudentEnrollment(studentId, testId);
        validateInstructionsRead(studentId, testId);

        // Check for existing exam session
        Optional<StudentExam> existingExam = studentExamRepository.findByStudentIdAndTestId(studentId, testId);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + testId));

        if (existingExam.isPresent()) {
            return handleExistingExam(existingExam.get(), studentId, testId);
        }

        return createNewExamSession(studentId, test);
    }

    /**
     * Retrieves paginated questions for an exam session with proper ordering
     *
     * @param studentId The student ID
     * @param testId The test ID
     * @param page Page number (0-based)
     * @param size Page size
     * @return Page of QuestionDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public Page<QuestionDto> getQuestions(Long studentId, Long testId, int page, int size) {
        validatePaginationParameters(page, size);

        Long sessionId = getSessionId(studentId, testId);
        PageRequest pageable = PageRequest.of(page, size);

        // Use ordered query for consistent question sequence
        Page<StudentExamQuestion> pageResult = studentExamQuestionRepository
                .findByStudentExam_SessionIdOrdered(sessionId, pageable);

        List<QuestionDto> dtos = pageResult.getContent()
                .stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList());

        log.debug("Retrieved {} questions for student {}, test {}, page {}",
                dtos.size(), studentId, testId, page);

        return new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
    }

    /**
     * Gets all question IDs for an exam session
     *
     * @param studentId The student ID
     * @param testId The test ID
     * @return List of question IDs in display order
     */
    @Override
    @Transactional(readOnly = true)
    public List<Long> getQuestionIds(Long studentId, Long testId) {
        Long sessionId = getSessionId(studentId, testId);
        List<Long> questionIds = studentExamQuestionRepository.findQuestionIdsBySessionId(sessionId);

        log.debug("Retrieved {} question IDs for student {}, test {}",
                questionIds.size(), studentId, testId);

        return questionIds;
    }

    /**
     * Saves student answers for multiple questions
     *
     * @param studentId The student ID
     * @param testId The test ID
     * @param answers List of StudentAnswerDto objects
     */
    @Override
    public void saveAnswers(Long studentId, Long testId, List<StudentAnswerDto> answers) {
        if (answers == null || answers.isEmpty()) {
            log.warn("Empty answers list provided for student {}, test {}", studentId, testId);
            return;
        }

        Long sessionId = getSessionId(studentId, testId);

        for (StudentAnswerDto dto : answers) {
            saveSingleAnswer(sessionId, dto);
        }

        log.info("Saved {} answers for student {}, test {}, session {}",
                answers.size(), studentId, testId, sessionId);
    }

    /**
     * Completes the exam session and triggers grading
     *
     * @param studentId The student ID
     * @param testId The test ID
     */
    @Override
    public void completeExam(Long studentId, Long testId) {
        Long sessionId = getSessionId(studentId, testId);

        StudentExam exam = studentExamRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with ID: " + sessionId));

        if (exam.getCompleted()) {
            throw new IllegalStateException("Session already completed: " + sessionId);
        }

        try {
            // Update exam status
            exam.setCompleted(true);
            exam.setEndTime(LocalDateTime.now());
            exam.setStatus(StudentExam.ExamStatus.SUBMITTED);
            exam.setTimeSpentSeconds(exam.getTimeSpent().getSeconds());

            // Grade the exam
            examGradingService.gradeExam(sessionId);
            exam.setGraded(true);

            studentExamRepository.save(exam);

            // Stop the timer
            unifiedWebSocketService.stopTimer(sessionId);

            log.info("Exam completed for student {}, test {}, session {}", studentId, testId, sessionId);

        } catch (Exception e) {
            log.error("Error completing exam for session {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to complete exam", e);
        }
    }

    /**
     * Gets questions filtered by type with proper ordering
     *
     * @param studentId The student ID
     * @param testId The test ID
     * @param type The question type to filter by
     * @param page Page number
     * @param size Page size
     * @return Page of QuestionDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public Page<QuestionDto> getQuestionsByType(Long studentId, Long testId, QuestionType type, int page, int size) {
        validatePaginationParameters(page, size);

        Long sessionId = getSessionId(studentId, testId);
        PageRequest pageable = PageRequest.of(page, size);

        Page<StudentExamQuestion> pageResult = studentExamQuestionRepository
                .findByStudentExam_SessionIdAndQuestion_TypeOrdered(sessionId, type, pageable);

        List<QuestionDto> dtos = pageResult.getContent().stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Validates student and test existence
     */
    private void validateStudentAndTest(Long studentId, Long testId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }

        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }

        if (!testRepository.existsById(testId)) {
            throw new IllegalArgumentException("Test not found with ID: " + testId);
        }
    }

    /**
     * Validates pagination parameters
     */
    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }

    /**
     * Validates student enrollment in the test
     */
    private void validateStudentEnrollment(Long studentId, Long testId) {
        boolean isEnrolled = enrolledTestsService.isStudentEnrolled(studentId, testId);
        if (!isEnrolled) {
            throw new IllegalStateException("Student is not enrolled in this test or test is not available");
        }
    }

    /**
     * Validates that student has read instructions
     */
    private void validateInstructionsRead(Long studentId, Long testId) {
        boolean hasReadInstructions = instructionsService.hasUserReadInstructions(studentId, testId);
        if (!hasReadInstructions) {
            throw new IllegalStateException("Student must read and acknowledge exam instructions before starting.");
        }
    }

    /**
     * Gets session ID for student and test combination
     */
    private Long getSessionId(Long studentId, Long testId) {
        return studentExamRepository.findByStudentIdAndTestId(studentId, testId)
                .map(StudentExam::getSessionId)
                .orElseThrow(() -> new RuntimeException(
                        "No active exam session found for student " + studentId + " and test " + testId));
    }

    /**
     * Handles existing exam session (resume or create new session)
     */
    private ExamSessionDto handleExistingExam(StudentExam existingExam, Long studentId, Long testId) {
        if (examSessionService.hasActiveSession(studentId, testId)) {
            log.info("Resuming existing active session for student {}, test {}", studentId, testId);
            return buildResponse(existingExam.getSessionId(), testId, "resumed");
        }

        log.info("Starting new session for existing exam record, student {}, test {}", studentId, testId);
        int duration = existingExam.getTest().getDurationMinutes();
        examSessionService.startSession(studentId, testId, duration);
        return buildResponse(existingExam.getSessionId(), testId, "resumed-new-session");
    }

    /**
     * Creates a new exam session with question assignment
     */
    private ExamSessionDto createNewExamSession(Long studentId, Test test) {
        log.info("Creating new exam session for student {}, test {}", studentId, test.getId());

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        // Create exam session
        StudentExam exam = StudentExam.builder()
                .studentId(studentId)
                .student(student)
                .test(test)
                .startTime(LocalDateTime.now())
                .completed(false)
                .graded(false)
                .status(StudentExam.ExamStatus.IN_PROGRESS)
                .currentQuestionIndex(0)
                .build();

        exam = studentExamRepository.save(exam);

        // Assign questions and start timer
        assignQuestionsToExam(exam, test);
        startExamTimer(exam, test);

        return buildNewSessionResponse(exam, test);
    }

    /**
     * Assigns questions to exam session with proper ordering
     */
    private void assignQuestionsToExam(StudentExam exam, Test test) {
        List<Question> allQuestions = questionRepository.findByTestId(test.getId());
        log.info("Fetched {} questions for test {}", allQuestions.size(), test.getId());

        // Apply randomization if enabled
        if (Boolean.TRUE.equals(test.getRandomizeQuestions())) {
            Collections.shuffle(allQuestions);
            log.info("Questions shuffled for test {}", test.getId());
        }

        // Select questions based on test configuration
        List<Question> selectedQuestions = allQuestions.stream()
                .limit(test.getNumberOfQuestions())
                .collect(Collectors.toList());

        // Assign questions with proper ordering
        int order = 1;
        for (Question question : selectedQuestions) {
            assignSingleQuestion(exam, question, test, order);
            order++;
        }

        log.info("Assigned {} questions to exam session {}", selectedQuestions.size(), exam.getSessionId());
    }

    /**
     * Assigns a single question to exam session
     */
    private void assignSingleQuestion(StudentExam exam, Question question, Test test, int order) {
        String originalChoices = question.getChoices();
        String shuffledChoices = originalChoices;

        // Apply choice shuffling if enabled
        if (Boolean.TRUE.equals(test.getShuffleChoices()) &&
                originalChoices != null &&
                !originalChoices.trim().isEmpty()) {
            shuffledChoices = shuffleChoices(originalChoices);
        }

        StudentExamQuestion examQuestion = StudentExamQuestion.builder()
                .studentExam(exam)
                .question(question)
                .answered(false)
                .shuffledChoices(shuffledChoices)
                .order(order)
                .build();

        studentExamQuestionRepository.save(examQuestion);

        log.debug("Assigned question {} (ID: {}) as position {} in exam session {}",
                question.getId(), examQuestion.getId(), order, exam.getSessionId());
    }

    /**
     * Starts exam timer
     */
    private void startExamTimer(StudentExam exam, Test test) {
        int durationMinutes = test.getDurationMinutes();
        int durationSeconds = durationMinutes * 60;
        unifiedWebSocketService.startTimer(exam.getSessionId(), durationSeconds);

        log.info("Started timer for session {}: {} minutes ({} seconds)",
                exam.getSessionId(), durationMinutes, durationSeconds);
    }

    /**
     * Saves a single answer with validation
     */
    private void saveSingleAnswer(Long sessionId, StudentAnswerDto dto) {
        StudentExamQuestion examQuestion = studentExamQuestionRepository.findById(dto.getStudentExamQuestionId())
                .orElseThrow(() -> new RuntimeException("Invalid question for this session"));

        validateQuestionOwnership(sessionId, examQuestion);
        saveOrUpdateAnswer(examQuestion, dto.getAnswer());
    }

    /**
     * Validates that question belongs to the session
     */
    private void validateQuestionOwnership(Long sessionId, StudentExamQuestion examQuestion) {
        if (!examQuestion.getStudentExam().getSessionId().equals(sessionId)) {
            throw new RuntimeException("Unauthorized answer submission: Question does not belong to this session");
        }
    }

    /**
     * Saves or updates an answer record
     */
    private void saveOrUpdateAnswer(StudentExamQuestion examQuestion, String answer) {
        StudentAnswer existingAnswer = answerRepository.findByStudentExamQuestion(examQuestion).orElse(null);

        if (existingAnswer == null) {
            existingAnswer = StudentAnswer.builder()
                    .question(examQuestion.getQuestion())
                    .studentExam(examQuestion.getStudentExam())
                    .studentExamQuestion(examQuestion)
                    .answer(answer)
                    .score(null)
                    .build();
        } else {
            existingAnswer.setAnswer(answer);
        }

        // Update exam question status
        examQuestion.setAnswered(true);
        examQuestion.setSavedAnswer(answer);

        studentExamQuestionRepository.save(examQuestion);
        answerRepository.save(existingAnswer);
    }

    /**
     * Converts StudentExamQuestion to QuestionDto for API response
     */
    private QuestionDto convertToQuestionDto(StudentExamQuestion examQuestion) {
        Question question = examQuestion.getQuestion();

        return QuestionDto.builder()
                .studentExamQuestionId(examQuestion.getId())
                .id(question.getId())
                .text(question.getText())
                .choices(examQuestion.getShuffledChoices())
                .correctAnswer(question.getCorrectAnswer())
                .type(question.getType())
                .maxMarks(question.getMaxMarks())
                .savedAnswer(examQuestion.getSavedAnswer())
                .mediaType(question.getMediaType())
                .mediaPath(question.getMediaPath())
                .mediaCaption(question.getMediaCaption())
                .questionOrder(examQuestion.getOrder())
                .build();
    }

    /**
     * Builds response for new session
     */
    private ExamSessionDto buildNewSessionResponse(StudentExam exam, Test test) {
        int questionCount = studentExamQuestionRepository.countByStudentExam_SessionId(exam.getSessionId()).intValue();

        return ExamSessionDto.builder()
                .sessionId(exam.getSessionId())
                .testId(test.getId())
                .studentId(exam.getStudentId())
                .totalQuestions(questionCount)
                .startTime(exam.getStartTime())
                .durationMinutes(test.getDurationMinutes())
                .status(exam.getStatus().toString())
                .currentQuestionIndex(exam.getCurrentQuestionIndex())
                .build();
    }

    /**
     * Builds generic response
     */
    private ExamSessionDto buildResponse(Long sessionId, Long testId, String status) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + testId));

        int questionCount = studentExamQuestionRepository.countByStudentExam_SessionId(sessionId).intValue();
        int durationMinutes = test.getDurationMinutes();

        return ExamSessionDto.builder()
                .sessionId(sessionId)
                .testId(testId)
                .durationMinutes(durationMinutes)
                .totalQuestions(questionCount)
                .status(status)
                .build();
    }

    /**
     * Shuffles answer choices for randomization
     */
    private String shuffleChoices(String choices) {
        if (choices == null || choices.trim().isEmpty()) {
            return choices;
        }

        try {
            List<String> choiceList = Arrays.asList(choices.split("\\s*,\\s*"));
            long seed = System.currentTimeMillis();
            Collections.shuffle(choiceList, new Random(seed));
            return String.join(",", choiceList);
        } catch (Exception e) {
            log.error("Error shuffling choices: {}", e.getMessage());
            return choices;
        }
    }
}