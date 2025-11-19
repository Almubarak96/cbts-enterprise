//package com.almubaraksuleiman.cbts.examiner.service;
//
//
//import com.almubaraksuleiman.cbts.dto.TestInstructions;
//
//import java.util.List;
//
//public interface TestInstructionsService {
//    TestInstructions getInstructions(String examId);
//    void saveInstructions(TestInstructions instructions);
//    boolean hasUserReadInstructions(String userId, String examId);
//    void markInstructionsAsRead(String userId, String examId);
//    List<TestInstructions> getAllInstructions();
//    void deleteInstructions(String examId);
//}


package com.almubaraksuleiman.cbts.examiner.service;

import com.almubaraksuleiman.cbts.dto.TestInstructions;

import java.util.List;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

public interface TestInstructionsService {
    TestInstructions getInstructionsForTest(Long testId);
    TestInstructions getInstructions(String examId); // Keep for backward compatibility
    void saveInstructions(TestInstructions instructions);

    // In TestInstructionsServiceImpl
    void updateInstructions(Long testId, TestInstructions updatedInstructions);

    boolean hasUserReadInstructions(Long userId, Long testId);
    void markInstructionsAsRead(Long userId, Long testId);
    List<TestInstructions> getAllInstructions();
    void deleteInstructions(Long testId);
    void deleteUserReadStatus(String userId, Long testId);

    // New methods for test-specific instructions
    TestInstructions createDefaultInstructionsForTest(Long testId);
    List<TestInstructions> getInstructionsForTests(List<Long> testIds);
}