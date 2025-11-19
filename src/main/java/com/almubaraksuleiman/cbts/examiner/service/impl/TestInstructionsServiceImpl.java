
package com.almubaraksuleiman.cbts.examiner.service.impl;

// Import necessary classes and packages
import com.almubaraksuleiman.cbts.dto.TestInstructions;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
import com.almubaraksuleiman.cbts.examiner.service.TestInstructionsService;
import com.almubaraksuleiman.cbts.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TestInstructionsServiceImpl - Service implementation for managing test instructions using Redis

 * This service handles resource, retrieval, and management of exam instructions with:
 * - Redis-based resource for high performance and scalability
 * - Test-specific instruction customization
 * - User read status tracking
 * - Default instruction generation based on test properties

 * @Service Marks this class as a Spring service component
 * @Slf4j Provides logging capabilities via Lombok
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Service
@Slf4j
public class TestInstructionsServiceImpl implements TestInstructionsService {

    // Redis hash operations for user read status (String -> String mapping)
    private final HashOperations<String, String, String> userStatusHashOps;

    // Redis hash operations for exam instructions (String -> TestInstructions mapping)
    private final HashOperations<String, String, TestInstructions> examInstructionsHashOps;

    // Template for default instructions that will be customized per test
    private final List<String> defaultInstructionTemplate;

    // Repository for accessing test data from database
    private final TestRepository testRepository;

    /**
     * Constructor for dependency injection
     *
     * @param userStatusHashOps Redis operations for user status tracking
     * @param examInstructionsHashOps Redis operations for exam instructions resource
     * @param defaultInstructionTemplate Template for default instructions
     * @param testRepository Repository for test data access
     */
    public TestInstructionsServiceImpl(
            HashOperations<String, String, String> userStatusHashOps,
            HashOperations<String, String, TestInstructions> examInstructionsHashOps,
            List<String> defaultInstructionTemplate,
            TestRepository testRepository) {
        this.userStatusHashOps = userStatusHashOps;
        this.examInstructionsHashOps = examInstructionsHashOps;
        this.defaultInstructionTemplate = defaultInstructionTemplate;
        this.testRepository = testRepository;
        log.info("TestInstructionsServiceImpl initialized with default instruction template");
    }

    /**
     * Retrieves instructions for a specific test from Redis
     * If instructions don't exist, creates and stores default instructions
     *
     * @param testId The ID of the test to retrieve instructions for
     * @return TestInstructions object containing the exam instructions
     * @apiNote Creates default instructions if none exist for the test
     */
    @Override
    public TestInstructions getInstructionsForTest(Long testId) {
        // Retrieve instructions from Redis using test ID as hash key
        TestInstructions instructions = examInstructionsHashOps.get(
                RedisConfig.TEST_INSTRUCTIONS_KEY,
                testId.toString());

        // If instructions not found in Redis, create default ones
        if (instructions == null) {
            log.info("No instructions found for test {}, creating default instructions", testId);
            instructions = createDefaultInstructionsForTest(testId);
            saveInstructions(instructions);
        }

        return instructions;
    }

    /**
     * Backward compatibility method that converts string examId to Long testId
     *
     * @param examId The exam ID as a string
     * @return TestInstructions object for the specified exam
     * @throws IllegalArgumentException if examId is not a valid numeric value
     */
    @Override
    public TestInstructions getInstructions(String examId) {
        try {
            // Convert string examId to Long testId
            Long testId = Long.parseLong(examId);
            return getInstructionsForTest(testId);
        } catch (NumberFormatException e) {
            log.error("Invalid exam ID format: {}", examId);
            throw new IllegalArgumentException("Exam ID must be a numeric value");
        }
    }

    /**
     * Saves instructions to Redis resource
     *
     * @param instructions The TestInstructions object to save
     * @throws IllegalArgumentException if instructions or testId is null
     */
    @Override
    public void saveInstructions(TestInstructions instructions) {
        // Validate input parameters
        if (instructions == null) {
            throw new IllegalArgumentException("Instructions cannot be null");
        }
        if (instructions.getTestId() == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }

        /*
        // Update timestamps (commented out as per your code)
        instructions.setUpdatedAt(LocalDateTime.now());
        if (instructions.getCreatedAt() == null) {
            instructions.setCreatedAt(LocalDateTime.now());
        }
        */

        // Store instructions in Redis with test ID as hash key
        examInstructionsHashOps.put(
                RedisConfig.TEST_INSTRUCTIONS_KEY,          // Redis hash key
                instructions.getTestId().toString(),        // Hash field (test ID)
                instructions                                // Hash value (instructions object)
        );
        log.info("Saved instructions for test {}", instructions.getTestId());
    }


    // In TestInstructionsServiceImpl
    @Override
    public void updateInstructions(Long testId, TestInstructions updatedInstructions) {
        TestInstructions existing = getInstructionsForTest(testId);

        // Update fields that are allowed to change
        existing.setInstructions(updatedInstructions.getInstructions());
        existing.setExamName(updatedInstructions.getExamName());
        existing.setDurationMinutes(updatedInstructions.getDurationMinutes());
        existing.setTotalQuestions(updatedInstructions.getTotalQuestions());
        existing.setPassingScore(updatedInstructions.getPassingScore());
        existing.setShuffleQuestions(updatedInstructions.isShuffleQuestions());
        existing.setShuffleChoices(updatedInstructions.isShuffleChoices());
        existing.setShowResultsImmediately(updatedInstructions.isShowResultsImmediately());

        saveInstructions(existing);
    }

    /**
     * Checks if a user has read the instructions for a specific test
     *
     * @param userId The ID of the user to check
     * @param testId The ID of the test to check
     * @return boolean indicating if user has read the instructions
     */
    @Override
    public boolean hasUserReadInstructions(Long userId, Long testId) {
        // Create composite key for user-test combination
        String hashKey = userId + ":" + testId;

        // Retrieve read status from Redis
        String readStatus = userStatusHashOps.get(RedisConfig.USER_READ_STATUS_KEY, hashKey);

        // Determine if user has read the instructions
        boolean hasRead = "true".equals(readStatus);
        log.debug("User {} read status for test {}: {}", userId, testId, hasRead);

        return hasRead;
    }

    /**
     * Marks instructions as read by a specific user for a specific test
     * Updates both user status and instruction read tracking
     *
     * @param userId The ID of the user who read the instructions
     * @param testId The ID of the test whose instructions were read
     */
    @Override
    public void markInstructionsAsRead(Long userId, Long testId) {
        // Create composite key for user-test combination
        String hashKey = userId + ":" + testId;

        // Store user read status in Redis
        userStatusHashOps.put(RedisConfig.USER_READ_STATUS_KEY, hashKey, "true");

        // Update the main instructions to track who read it
        TestInstructions instructions = getInstructionsForTest(testId);

        // Initialize readBy list if null
        if (instructions.getReadBy() == null) {
            instructions.setReadBy(new ArrayList<>());
        }

        // Add user to readBy list if not already present
        if (!instructions.getReadBy().contains(userId)) {
            instructions.getReadBy().add(String.valueOf(userId));
            saveInstructions(instructions);
        }

        log.info("User {} marked instructions as read for test {}", userId, testId);
    }

    /**
     * Retrieves all instructions stored in Redis
     *
     * @return List of all TestInstructions objects
     */
    @Override
    public List<TestInstructions> getAllInstructions() {
        // Get all values from the instructions hash
        List<TestInstructions> allInstructions = examInstructionsHashOps.values(RedisConfig.TEST_INSTRUCTIONS_KEY);
        log.debug("Retrieved {} instructions from Redis", allInstructions.size());
        return allInstructions;
    }

    /**
     * Deletes instructions for a specific test from Redis
     *
     * @param testId The ID of the test whose instructions should be deleted
     */
    @Override
    public void deleteInstructions(Long testId) {
        // Remove instructions from Redis hash
        examInstructionsHashOps.delete(RedisConfig.TEST_INSTRUCTIONS_KEY, testId.toString());
        log.info("Deleted instructions for test {}", testId);
    }

    /**
     * Deletes the read status for a specific user and test combination
     *
     * @param userId The ID of the user whose status should be deleted
     * @param testId The ID of the test whose status should be deleted
     */
    @Override
    public void deleteUserReadStatus(String userId, Long testId) {
        // Create composite key for user-test combination
        String hashKey = userId + ":" + testId;

        // Remove user read status from Redis
        userStatusHashOps.delete(RedisConfig.USER_READ_STATUS_KEY, hashKey);
        log.info("Deleted read status for user {} and test {}", userId, testId);
    }

    /**
     * Creates default instructions specifically tailored for a test
     * Uses test properties from database to customize instructions
     *
     * @param testId The ID of the test to create instructions for
     * @return TestInstructions object with default values based on test properties
     * @throws IllegalArgumentException if test is not found
     */
    @Override
    public TestInstructions createDefaultInstructionsForTest(Long testId) {
        // Retrieve test details from database
        Optional<Test> testOptional = testRepository.findById(testId);

        // Check if test exists
        if (testOptional.isEmpty()) {
            throw new IllegalArgumentException("Test not found with ID: " + testId);
        }

        Test test = testOptional.get();

        // Build and return customized instructions
        return TestInstructions.builder()
                .testId(testId)                                   // Associate with specific test
                .examName("Test " + test.getId())                 // Use test ID in name
                .instructions(customizeInstructionsForTest(testId, defaultInstructionTemplate)) // Customized content
                .durationMinutes(test.getDurationMinutes())       // Use test's actual duration
                .totalQuestions(test.getNumberOfQuestions())      // Use test's question count
                .passingScore(70)                                 // Default passing score
                .shuffleQuestions(test.getRandomizeQuestions())   // Use test's shuffle setting
                .shuffleChoices(test.getShuffleChoices())         // Use test's choice shuffle setting
                .showResultsImmediately(false)                    // Default result display setting
                //.createdAt(LocalDateTime.now())                 // Timestamp (commented out)
                //.updatedAt(LocalDateTime.now())                 // Timestamp (commented out)
                .readBy(new ArrayList<>())                        // Initialize empty read list
                .build();
    }

    /**
     * Customizes default instructions based on test properties
     * Replaces placeholders with actual test-specific values
     *
     * @param testId The ID of the test to customize instructions for
     * @param template The instruction template to customize
     * @return List of customized instruction strings
     */
    private List<String> customizeInstructionsForTest(Long testId, List<String> template) {
        // Retrieve test details for customization
        Optional<Test> testOptional = testRepository.findById(testId);

        // Customize each instruction in the template
        return template.stream()
                .map(instruction -> customizeInstruction(instruction, testId, testOptional))
                .collect(Collectors.toList());
    }

    /**
     * Customizes individual instruction based on test properties
     * Replaces placeholders with actual values from test
     *
     * @param instruction The instruction template to customize
     * @param testId The ID of the test
     * @param testOptional Optional containing test details
     * @return Customized instruction string
     */
    private String customizeInstruction(String instruction, Long testId, Optional<Test> testOptional) {
        // Use test properties if available, otherwise use defaults
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + testId));

            // Replace placeholders with actual test values
            if (instruction.contains("%d minutes")) {
                return String.format(instruction, test.getDurationMinutes());
            } else if (instruction.contains("%d questions")) {
                return String.format(instruction, test.getNumberOfQuestions());
//            } else if (instruction.contains("%d%%")) {
//                return String.format(instruction, test.getPassingScore() != null ? test.getPassingScore() : 70);
            } else if (instruction.contains("presented %s"))
            {
                String order = Boolean.TRUE.equals(test.getRandomizeQuestions()) ?
                        "in random order" : "in sequential order";
                return String.format(instruction, order);
            } else if (instruction.contains("choices will be %s")) {
                String shuffle = Boolean.TRUE.equals(test.getShuffleChoices()) ?
                        "randomized" : "in fixed order";
                return String.format(instruction, shuffle);
            }


        // Return original instruction if no placeholders matched
        return instruction;
    }

    /**
     * Overloaded version of customizeInstruction for convenience
     */
    private String customizeInstruction(String instruction, Long testId) {
        Optional<Test> testOptional = testRepository.findById(testId);
        return customizeInstruction(instruction, testId, testOptional);
    }

    /**
     * Retrieves instructions for multiple tests in a single call
     *
     * @param testIds List of test IDs to retrieve instructions for
     * @return List of TestInstructions objects for the specified tests
     */
    @Override
    public List<TestInstructions> getInstructionsForTests(List<Long> testIds) {
        // Stream through test IDs and retrieve instructions for each
        return testIds.stream()
                .map(this::getInstructionsForTest)          // Get instructions for each test
                .collect(Collectors.toList());              // Collect into list
    }

    /**
     * Bulk save instructions for multiple tests
     * More efficient than individual saves for large batches
     *
     * @param instructionsList List of TestInstructions objects to save
     */
    public void saveAllInstructions(List<TestInstructions> instructionsList) {
        // Process each instruction in the list
        instructionsList.forEach(instructions -> {
            // Only save if test ID is present
            if (instructions.getTestId() != null) {
                examInstructionsHashOps.put(
                        RedisConfig.TEST_INSTRUCTIONS_KEY,          // Redis hash key
                        instructions.getTestId().toString(),        // Hash field (test ID)
                        instructions                                // Hash value (instructions)
                );
            }
        });
        log.info("Saved instructions for {} tests", instructionsList.size());
    }

    /**
     * Checks if multiple users have read instructions for a specific test
     *
     * @param userIds List of user IDs to check
     * @param testId The ID of the test to check
     * @return List of booleans indicating read status for each user
     */
    public List<Boolean> hasUsersReadInstructions(List<Long> userIds, Long testId) {
        // Check read status for each user
        return userIds.stream()
                .map(userId -> hasUserReadInstructions(userId, testId))  // Check each user
                .collect(Collectors.toList());                           // Collect results
    }
}