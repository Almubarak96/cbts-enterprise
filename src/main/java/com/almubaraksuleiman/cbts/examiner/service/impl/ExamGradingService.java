//package com.almubaraksuleiman.cbts.examiner.service.impl;
//
//import com.almubaraksuleiman.cbts.examiner.model.Question;
//import com.almubaraksuleiman.cbts.student.model.StudentAnswer;
//import com.almubaraksuleiman.cbts.student.model.StudentExam;
//import com.almubaraksuleiman.cbts.student.repository.StudentAnswerRepository;
//import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ExamGradingService {
//
//    private final StudentExamRepository studentExamRepository;
//    private final StudentAnswerRepository answerRepository;
//
//    public double gradeExam(Long sessionId) {
//        StudentExam exam = studentExamRepository.findById(sessionId)
//                .orElseThrow(() -> new RuntimeException("Session not found"));
//
//        double totalScore = 0.0;
//
//        for (StudentAnswer answer : answerRepository.findByStudentExam(exam)) {
//            Question question = answer.getStudentExamQuestion().getQuestion();
//
//            double score = 0.0;
//
//            switch (question.getType()) {
//                case MULTIPLE_CHOICE:
//                case FILL_IN_THE_BLANK:
//                    if (answer.getAnswer() != null &&
//                        answer.getAnswer().trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
//                        score = question.getMaxMarks();
//                    }
//                    break;
//
//                case MULTIPLE_SELECT:
//                    // Compare sets instead of strings
//                    List<String> correctSet = Arrays.stream(question.getCorrectAnswer().split(","))
//                            .map(String::trim).toList();
//                    List<String> givenSet = Arrays.stream(answer.getAnswer().split(","))
//                            .map(String::trim).toList();
//
//                    if (correctSet.size() == givenSet.size() && correctSet.containsAll(givenSet)) {
//                        score = question.getMaxMarks();
//                    }
//                    break;
//
//                case ESSAY:
//                    // Essay grading can be manual → score stays null
//                    score = 0.0;
//                    break;
//            }
//
//            answer.setScore(score); // save per-question score
//            answerRepository.save(answer);
//            totalScore += score;
//        }
//
//        exam.setGraded(true);
//        exam.setScore((int) totalScore);
//        studentExamRepository.save(exam);
//
//        return totalScore;
//    }
//}


package com.almubaraksuleiman.cbts.examiner.service.impl;

// Import statements organized by functionality
import com.almubaraksuleiman.cbts.examiner.model.Question;
import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import com.almubaraksuleiman.cbts.student.model.StudentAnswer;
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.repository.StudentAnswerRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ExamGradingService - Service for automatic exam grading and score calculation
 * This service handles the automatic grading of completed exams by:
 * - Evaluating different question types (multiple choice, multiple select, true or false,  fill-in-blank, essay)
 * - Calculating scores based on correct answers
 * - Updating student answer records with individual scores
 * - Updating the overall exam score and graded status
 * - Handling various answer formats and normalization
 *
 * @Service Marks this class as a Spring service bean
 * @RequiredArgsConstructor Lombok: generates constructor for final fields
 * @Slf4j Lombok: provides logger instance for logging
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamGradingService {

    // REPOSITORY DEPENDENCIES
    /** For student exam data access and updates */
    private final StudentExamRepository studentExamRepository;

    /** For student answer data access and score updates */
    private final StudentAnswerRepository answerRepository;

    /**
     * Grades an entire exam session by evaluating all student answers
     *
     * @param sessionId The ID of the exam session to grade
     * @return double The total score achieved by the student
     * @throws RuntimeException if the exam session is not found
     * @apiNote This method automatically handles different question types and updates both
     *          individual answer scores and the overall exam score
     */
//    public double gradeExam(Long sessionId) {
//        log.info("Starting automatic grading for exam session: {}", sessionId);
//
//        // Retrieve the exam session with validation
//        StudentExam exam = studentExamRepository.findById(sessionId)
//                .orElseThrow(() -> {
//                    log.error("Exam session not found for grading: {}", sessionId);
//                    return new RuntimeException("Session not found with ID: " + sessionId);
//                });
//
//        double totalScore = 0.0;
//        int gradedQuestions = 0;
//
//        // Retrieve all answers for this exam session
//        List<StudentAnswer> answers = answerRepository.findByStudentExam(exam);
//
//        log.debug("Found {} answers to grade for session {}", answers.size(), sessionId);
//
//        // Grade each answer individually
//        for (StudentAnswer answer : answers) {
//            double questionScore = gradeSingleAnswer(answer);
//            totalScore += questionScore;
//
//            if (questionScore > 0) {
//                gradedQuestions++;
//            }
//        }
//
//        // Update exam with final score and status
//        updateExamResults(exam, totalScore, gradedQuestions, answers.size());
//
//        log.info("Completed grading session {}. Total score: {}. Graded {}/{} questions.",
//                sessionId, totalScore, gradedQuestions, answers.size());
//
//        return totalScore;
//    }










    /**
     * Enhanced gradeExam method to ensure essay scores are included in final calculation
     */
    //@Override
    public double gradeExam(Long sessionId) {
        log.info("Starting enhanced automatic grading for exam session: {}", sessionId);

        StudentExam exam = studentExamRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("Exam session not found for grading: {}", sessionId);
                    return new RuntimeException("Session not found with ID: " + sessionId);
                });

        double totalScore = 0.0;
        int gradedQuestions = 0;
        boolean hasEssays = false;

        List<StudentAnswer> answers = answerRepository.findByStudentExam(exam);
        log.debug("Found {} answers to grade for session {}", answers.size(), sessionId);

        for (StudentAnswer answer : answers) {
            Question question = answer.getStudentExamQuestion().getQuestion();
            double questionScore = 0.0;

            try {
                switch (question.getType()) {
                    case MULTIPLE_CHOICE:
                        questionScore = gradeMultipleChoice(answer, question);
                        break;

                    case MULTIPLE_SELECT:
                        questionScore = gradeMultipleSelect(answer, question);
                        break;

                    case TRUE_FALSE:
                        questionScore = gradeTrueFalse(answer, question);
                        break;

                    case FILL_IN_THE_BLANK:
                        questionScore = gradeFillInTheBlank(answer, question);
                        break;

                    case ESSAY:
                        // ESSAY GRADING - Use manually assigned score if available
                        if (answer.getScore() != null && answer.getScore() > 0) {
                            questionScore = answer.getScore();
                            log.debug("Using manual essay score for question {}: {}", question.getId(), questionScore);
                        } else {
                            questionScore = 0.0; // Essay not graded yet
                            log.debug("Essay question {} not graded yet, score set to 0", question.getId());
                        }
                        hasEssays = true;
                        break;

                    default:
                        log.warn("Unknown question type: {} for question ID: {}", question.getType(), question.getId());
                        questionScore = 0.0;
                }
            } catch (Exception e) {
                log.error("Error grading answer for question {}: {}", question.getId(), e.getMessage());
                questionScore = 0.0;
            }

            // Update the answer score if it's different
            if (!Objects.equals(answer.getScore(), questionScore)) {
                answer.setScore(questionScore);
                answerRepository.save(answer);
            }

            totalScore += questionScore;

            if (questionScore > 0) {
                gradedQuestions++;
            }
        }

        // Update exam with final score and status
        updateExamResults(exam, totalScore, gradedQuestions, answers.size(), hasEssays);

        log.info("Completed enhanced grading session {}. Total score: {}. Graded {}/{} questions. Has essays: {}",
                sessionId, totalScore, gradedQuestions, answers.size(), hasEssays);

        return totalScore;
    }

    /**
     * Enhanced exam results update
     */
    private void updateExamResults(StudentExam exam, double totalScore, int gradedQuestions, int totalQuestions, boolean hasEssays) {
        exam.setGraded(true);
        exam.setScore((int) totalScore);

        // Calculate percentage based on maximum possible score
        double maxPossibleScore = calculateMaxPossibleScore(exam);
        if (maxPossibleScore > 0) {
            double percentage = (totalScore / maxPossibleScore) * 100;
            exam.setPercentage(Math.round(percentage * 100.0) / 100.0); // Round to 2 decimal places
        }

        // Set appropriate status based on grading completion
        if (hasEssays) {
            // Check if all essays are graded
            long ungradedEssays = answerRepository.findByStudentExam(exam).stream()
                    .filter(answer -> answer.getQuestion().getType() == QuestionType.ESSAY)
                    .filter(answer -> answer.getScore() == null || answer.getScore() == 0)
                    .count();

            if (ungradedEssays > 0) {
               // exam.setStatus("PARTIALLY_GRADED");
                exam.setStatus(StudentExam.ExamStatus.PARTIALLY_GRADED);

                log.info("Exam {} partially graded. {} essays still pending.", exam.getSessionId(), ungradedEssays);
            } else {
                exam.setStatus(StudentExam.ExamStatus.FULLY_GRADED);

                //exam.setStatus("FULLY_GRADED");
                log.info("Exam {} fully graded including all essays.", exam.getSessionId());
            }
        } else {
            exam.setStatus(StudentExam.ExamStatus.FULLY_GRADED);

            // exam.setStatus("FULLY_GRADED");
        }

        studentExamRepository.save(exam);

        log.info("Exam {} graded. Score: {}/{} ({}% auto-graded), Status: {}",
                exam.getSessionId(), totalScore, maxPossibleScore,
                (gradedQuestions * 100) / totalQuestions, exam.getStatus());
    }




    /**
     * Check if all essays in an exam session have been graded
     */
    public boolean areAllEssaysGraded(Long sessionId) {
        long ungradedEssays = answerRepository.findBySessionIdAndQuestionType(sessionId, QuestionType.ESSAY)
                .stream()
                .filter(answer -> answer.getScore() == null || answer.getScore() == 0)
                .count();

        return ungradedEssays == 0;
    }

    /**
     * Get exam grading status with essay information
     */
    public Map<String, Object> getExamGradingStatus(Long sessionId) {
        StudentExam exam = studentExamRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        List<StudentAnswer> allAnswers = answerRepository.findByStudentExam(exam);
        List<StudentAnswer> essayAnswers = allAnswers.stream()
                .filter(answer -> answer.getQuestion().getType() == QuestionType.ESSAY)
                .collect(Collectors.toList());

        long gradedEssays = essayAnswers.stream()
                .filter(answer -> answer.getScore() != null && answer.getScore() > 0)
                .count();

        Map<String, Object> status = new HashMap<>();
        status.put("sessionId", sessionId);
        status.put("totalScore", exam.getScore());
        status.put("percentage", exam.getPercentage());
        status.put("graded", exam.getGraded());
        status.put("status", exam.getStatus());
        status.put("totalEssays", essayAnswers.size());
        status.put("gradedEssays", gradedEssays);
        status.put("allEssaysGraded", gradedEssays == essayAnswers.size());

        return status;
    }






    /**
     * Grades a single student answer based on question type and correctness
     *
     * @param answer The StudentAnswer entity to grade
     * @return double The score achieved for this question (0 if incorrect or not gradable)
     */
    private double gradeSingleAnswer(StudentAnswer answer) {
        Question question = answer.getStudentExamQuestion().getQuestion();
        double score = 0.0;

        try {
            switch (question.getType()) {
                case MULTIPLE_CHOICE:
                    score = gradeMultipleChoice(answer, question);
                    break;

                case MULTIPLE_SELECT:
                    score = gradeMultipleSelect(answer, question);
                    break;

                case TRUE_FALSE:
                    score = gradeTrueFalse(answer, question);
                    break;

                case FILL_IN_THE_BLANK:
                    score = gradeFillInTheBlank(answer, question);
                    break;

                case ESSAY:
                    score = gradeEssay(answer, question);
                    break;

                default:
                    log.warn("Unknown question type: {} for question ID: {}",
                            question.getType(), question.getId());
                    score = 0.0;
            }
        } catch (Exception e) {
            log.error("Error grading answer for question {}: {}", question.getId(), e.getMessage());
            score = 0.0; // Safe fallback on grading errors
        }

        // Update and save the answer score
        answer.setScore(score);
        answerRepository.save(answer);

        log.debug("Graded question {}: score {}", question.getId(), score);
        return score;
    }

    /**
     * Grades a multiple choice question
     *
     * @param answer The student's answer
     * @param question The question with correct answer
     * @return double Full marks if correct, 0 otherwise
     */
    private double gradeMultipleChoice(StudentAnswer answer, Question question) {
        if (answer.getAnswer() != null &&
                answer.getAnswer().trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
            return question.getMaxMarks();
        }
        return 0.0;
    }


    /**
     * Grades a TRUE_FALSE
     *
     * @param answer The student's answer
     * @param question The question with correct answer
     * @return double Full marks if correct, 0 otherwise
     */
    private double gradeTrueFalse(StudentAnswer answer, Question question) {
        if (answer.getAnswer() != null &&
                answer.getAnswer().trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
            return question.getMaxMarks();
        }
        return 0.0;
    }

    /**
     * Grades a fill-in-the-blank question (case-insensitive)
     *
     * @param answer The student's answer
     * @param question The question with correct answer
     * @return double Full marks if correct, 0 otherwise
     */
    private double gradeFillInTheBlank(StudentAnswer answer, Question question) {
        if (answer.getAnswer() != null &&
                answer.getAnswer().trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
            return question.getMaxMarks();
        }
        return 0.0;
    }

    /**
     * Grades a multiple select question (order-independent comparison)
     *
     * @param answer The student's answer (comma-separated values)
     * @param question The question with correct answers (comma-separated)
     * @return double Full marks if all selections are correct, 0 otherwise
     */
    private double gradeMultipleSelect(StudentAnswer answer, Question question) {
        if (answer.getAnswer() == null || question.getCorrectAnswer() == null) {
            return 0.0;
        }

        // Normalize and compare answer sets
        List<String> correctAnswers = Arrays.stream(question.getCorrectAnswer().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        List<String> studentAnswers = Arrays.stream(answer.getAnswer().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        // Check if sets match (order doesn't matter, but all must be present)
        if (correctAnswers.size() == studentAnswers.size() &&
                correctAnswers.containsAll(studentAnswers)) {
            return question.getMaxMarks();
        }

        return 0.0;
    }

    /**
     * Handles essay question grading
     *
     * @param answer The student's essay answer
     * @param question The essay question
     * @return double Always returns 0 as essays require manual grading
     * @apiNote Essay questions require manual grading. This method serves as a placeholder
     *          and should be extended for automated essay scoring if needed.
     */
    private double gradeEssay(StudentAnswer answer, Question question) {
        log.debug("Essay question {} requires manual grading. Score set to 0.", question.getId());
        // Essay questions typically require manual grading

        return 0.0;
    }

    /**
     * Updates the exam with final results and grading status
     *
     * @param exam The StudentExam entity to update
     * @param totalScore The total score achieved
     * @param gradedQuestions Number of questions that were automatically graded
     * @param totalQuestions Total number of questions in the exam
     */
    private void updateExamResults(StudentExam exam, double totalScore, int gradedQuestions, int totalQuestions) {
        exam.setGraded(true);
        exam.setScore((int) totalScore);

        // Calculate percentage if needed
        double maxPossibleScore = calculateMaxPossibleScore(exam);
        if (maxPossibleScore > 0) {
            double percentage = (totalScore / maxPossibleScore) * 100;
            exam.setPercentage(percentage);
        }

        studentExamRepository.save(exam);

        log.info("Exam {} graded. Score: {}/{} ({}% auto-graded)",
                exam.getSessionId(), totalScore, maxPossibleScore,
                (gradedQuestions * 100) / totalQuestions);
    }

    /**
     * Calculates the maximum possible score for the exam
     *
     * @param exam The StudentExam entity
     * @return double The sum of max marks for all questions
     */
    private double calculateMaxPossibleScore(StudentExam exam) {
        return answerRepository.findByStudentExam(exam).stream()
                .mapToDouble(answer -> answer.getStudentExamQuestion().getQuestion().getMaxMarks())
                .sum();
    }

    /**
     * Gets the grading status of an exam
     *
     * @param sessionId The exam session ID
     * @return boolean True if the exam has been graded, false otherwise
     */
    public boolean isExamGraded(Long sessionId) {
        return studentExamRepository.findById(sessionId)
                .map(StudentExam::getGraded)
                .orElse(false);
    }

    /**
     * Gets the final score of a graded exam
     *
     * @param sessionId The exam session ID
     * @return Integer The exam score, or null if not graded
     * @throws RuntimeException if exam not found
     */
    public Integer getExamScore(Long sessionId) {
        StudentExam exam = studentExamRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        return exam.getGraded() ? exam.getScore() : null;
    }

    /**
     * Gets the percentage score of a graded exam
     *
     * @param sessionId The exam session ID
     * @return Double The percentage score, or null if not graded
     * @throws RuntimeException if exam not found
     */
    public Double getExamPercentage(Long sessionId) {
        StudentExam exam = studentExamRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        return exam.getGraded() ? exam.getPercentage() : null;
    }
}