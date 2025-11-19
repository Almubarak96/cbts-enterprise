package com.almubaraksuleiman.cbts.examiner.service.impl;

import com.almubaraksuleiman.cbts.examiner.dto.EssayGradingResponse;
import com.almubaraksuleiman.cbts.examiner.dto.ManualGradingRequest;
import com.almubaraksuleiman.cbts.examiner.dto.StudentEssayAnswerDto;
import com.almubaraksuleiman.cbts.examiner.dto.StudentEssayGroupDto;
import com.almubaraksuleiman.cbts.examiner.model.Question;
import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import com.almubaraksuleiman.cbts.examiner.service.ManualGradingService;
import com.almubaraksuleiman.cbts.student.model.Student;
import com.almubaraksuleiman.cbts.student.model.StudentAnswer;
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.repository.StudentAnswerRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManualGradingServiceImpl implements ManualGradingService {

    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentExamRepository studentExamRepository;
    private final StudentRepository studentRepository;
    private final ExamGradingService examGradingService;

    @Override
    @Transactional
    public EssayGradingResponse gradeEssayAnswer(ManualGradingRequest request) {
        try {
            log.info("Starting manual grading for session: {}, question: {}", 
                     request.getSessionId(), request.getQuestionId());
            
            // Validate the score
            if (request.getScore() == null || request.getScore() < 0) {
                throw new IllegalArgumentException("Score must be a non-negative number");
            }

            // Find the student answer
            StudentAnswer studentAnswer = studentAnswerRepository
                    .findBySessionIdAndQuestionId(request.getSessionId(), request.getQuestionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Student answer not found for session: " + request.getSessionId() + 
                            " and question: " + request.getQuestionId()));

            // Validate it's an essay question
            Question question = studentAnswer.getQuestion();
            if (question.getType() != QuestionType.ESSAY) {
                throw new IllegalArgumentException("Only essay questions can be manually graded");
            }

            // Validate score doesn't exceed max marks
            if (request.getScore() > question.getMaxMarks()) {
                throw new IllegalArgumentException(
                        "Score cannot exceed maximum marks: " + question.getMaxMarks());
            }

            // Update the answer with manual grade
            studentAnswer.setScore(request.getScore());
            // You might want to store feedback in a separate field or extend the entity
            studentAnswerRepository.save(studentAnswer);

            log.info("Successfully graded essay. Session: {}, Question: {}, Score: {}/{}", 
                     request.getSessionId(), request.getQuestionId(), 
                     request.getScore(), question.getMaxMarks());

            // Recalculate total exam score
            recalculateExamScore(request.getSessionId());

            return EssayGradingResponse.builder()
                    .sessionId(request.getSessionId())
                    .questionId(request.getQuestionId())
                    .awardedScore(request.getScore())
                    .maxMarks(question.getMaxMarks())
                    .feedback(request.getFeedback())
                    .gradedAt(LocalDateTime.now())
                    .success(true)
                    .message("Essay graded successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error grading essay answer: {}", e.getMessage(), e);
            return EssayGradingResponse.builder()
                    .sessionId(request.getSessionId())
                    .questionId(request.getQuestionId())
                    .success(false)
                    .message("Failed to grade essay: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Page<StudentEssayAnswerDto> getUngradedEssays(Long testId, Pageable pageable) {
        Page<StudentAnswer> ungradedEssays = studentAnswerRepository
                .findUngradedEssaysByTestId(testId, pageable);
        
        return ungradedEssays.map(this::convertToStudentEssayAnswerDto);
    }

    @Override
    public Page<StudentEssayAnswerDto> getAllEssays(Long testId, Pageable pageable) {
        Page<StudentAnswer> allEssays = studentAnswerRepository
                .findAllEssaysByTestId(testId, pageable);
        
        return allEssays.map(this::convertToStudentEssayAnswerDto);
    }

    @Override
    public StudentEssayAnswerDto getEssayAnswer(Long sessionId, Long questionId) {
        StudentAnswer studentAnswer = studentAnswerRepository
                .findBySessionIdAndQuestionId(sessionId, questionId)
                .orElseThrow(() -> new RuntimeException(
                        "Essay answer not found for session: " + sessionId + 
                        " and question: " + questionId));

        return convertToStudentEssayAnswerDto(studentAnswer);
    }

    @Override
    public Long countUngradedEssays(Long testId) {
        return studentAnswerRepository.countUngradedEssaysByTestId(testId);
    }

    @Override
    public Long countGradedEssays(Long testId) {
        return studentAnswerRepository.countGradedEssaysByTestId(testId);
    }

    @Override
    public List<StudentEssayGroupDto> getEssaysGroupedByStudent(Long testId) {
        log.info("Fetching essays grouped by student for test: {}", testId);

        // Get all essays for the test (without pagination for grouping)
        List<StudentAnswer> allEssays = studentAnswerRepository.findAllEssaysByTestId(testId);

        // Group by student
        Map<Long, StudentEssayGroupDto> groups = new HashMap<>();

        for (StudentAnswer essay : allEssays) {
            StudentExam studentExam = essay.getStudentExam();
            Student student = studentRepository.findById(studentExam.getStudentId())
                    .orElse(Student.builder()
                            .firstName("Unknown")
                            .lastName("Student")
                            .email("unknown@student.edu")
                            .build());

            Long studentId = student.getId();

            // Create group if it doesn't exist
            if (!groups.containsKey(studentId)) {
                groups.put(studentId, StudentEssayGroupDto.builder()
                        .studentId(studentId)
                        .studentName(student.getFirstName() + " " + student.getLastName())
                        .studentEmail(student.getEmail())
                        .essays(new ArrayList<>())
                        .gradedCount(0)
                        .totalCount(0)
                        .completionPercentage(0.0)
                        .build());
            }

            StudentEssayGroupDto group = groups.get(studentId);
            StudentEssayAnswerDto essayDto = convertToStudentEssayAnswerDto(essay);
            group.getEssays().add(essayDto);
            group.setTotalCount(group.getTotalCount() + 1);

            if (essayDto.isGraded()) {
                group.setGradedCount(group.getGradedCount() + 1);
            }
        }

        // Calculate completion percentages
        for (StudentEssayGroupDto group : groups.values()) {
            if (group.getTotalCount() > 0) {
                double percentage = (double) group.getGradedCount() / group.getTotalCount() * 100;
                group.setCompletionPercentage(Math.round(percentage * 100.0) / 100.0);
            }
        }

        // Sort groups by student name
        List<StudentEssayGroupDto> result = new ArrayList<>(groups.values());
        result.sort(Comparator.comparing(StudentEssayGroupDto::getStudentName));

        log.info("Found {} student groups for test {}", result.size(), testId);
        return result;
    }

    private StudentEssayAnswerDto convertToStudentEssayAnswerDto(StudentAnswer studentAnswer) {
        StudentExam studentExam = studentAnswer.getStudentExam();
        Question question = studentAnswer.getQuestion();
        
        // Get student details
        Student student = studentRepository.findById(studentExam.getStudentId())
                .orElse(Student.builder()
                        .firstName("Unknown")
                        .lastName("Student")
                        .email("unknown@student.edu")
                        .build());

        return StudentEssayAnswerDto.builder()
                .sessionId(studentExam.getSessionId())
                .studentId(studentExam.getStudentId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .studentEmail(student.getEmail())
                .questionId(question.getId())
                .questionText(question.getText())
                .essayAnswer(studentAnswer.getAnswer())
                .maxMarks(question.getMaxMarks())
                .currentScore(studentAnswer.getScore())
                .graded(studentAnswer.getScore() != null && studentAnswer.getScore() > 0)
                .submittedAt(studentExam.getEndTime())
                .build();
    }

    private void recalculateExamScore(Long sessionId) {
        try {
            // Use the existing grading service to recalculate total score
            examGradingService.gradeExam(sessionId);
            log.info("Recalculated total exam score for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error recalculating exam score for session {}: {}", sessionId, e.getMessage());
            // Don't fail the manual grading if recalculation fails
        }
    }
}