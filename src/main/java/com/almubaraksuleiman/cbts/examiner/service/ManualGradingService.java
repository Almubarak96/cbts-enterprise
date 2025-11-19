package com.almubaraksuleiman.cbts.examiner.service;


import com.almubaraksuleiman.cbts.examiner.dto.EssayGradingResponse;
import com.almubaraksuleiman.cbts.examiner.dto.ManualGradingRequest;
import com.almubaraksuleiman.cbts.examiner.dto.StudentEssayAnswerDto;
import com.almubaraksuleiman.cbts.examiner.dto.StudentEssayGroupDto;
import com.almubaraksuleiman.cbts.student.model.StudentAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ManualGradingService {
    EssayGradingResponse gradeEssayAnswer(ManualGradingRequest request);
    Page<StudentEssayAnswerDto> getUngradedEssays(Long testId, Pageable pageable);
    Page<StudentEssayAnswerDto> getAllEssays(Long testId, Pageable pageable);
    StudentEssayAnswerDto getEssayAnswer(Long sessionId, Long questionId);
    Long countUngradedEssays(Long testId);
    Long countGradedEssays(Long testId);

    // Add this method to ManualGradingService interface
    List<StudentEssayGroupDto> getEssaysGroupedByStudent(Long testId);
}