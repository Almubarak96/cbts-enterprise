// Package declaration - organizes classes within the service package
package com.almubaraksuleiman.cbts.examiner.service;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.dto.TestDto;
import com.almubaraksuleiman.cbts.examiner.service.impl.TestServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Test management operations.
 * Defines the contract for business logic related to test creation, retrieval,
 * modification, deletion, and bulk operations.

 * This interface follows the Service Layer pattern, separating business logic
 * from controller and repository layers.

 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

public interface TestService {

    /**
     * Creates a new test from the provided data transfer object.
     * Performs validation, converts DTO to entity, and persists to database.
     *
     * @param testDto Data Transfer Object containing test information
     * @return TestDto The created test with generated ID and persisted data
     * @throws IllegalArgumentException if validation fails or data is invalid
     */
    TestDto createTest(TestDto testDto);

    /**
     * Retrieves all tests from the system.
     * Returns a list of all available tests converted to DTOs.
     *
     * @return List<TestDto> List of all tests in the system
     *         Returns empty list if no tests exist
     */
    List<TestDto> getAllTests();

    /**
     * Retrieves a specific test by its unique identifier.
     *
     * @param id The unique ID of the test to retrieve
     * @return TestDto The test data transfer object
     * @throws IllegalArgumentException if no test exists with the given ID
     */
    TestDto getTestById(Long id);

    /**
     * Deletes a test by its unique identifier.
     * Removes the test and potentially associated questions (depending on cascade settings).
     *
     * @param id The unique ID of the test to delete
     * @throws IllegalArgumentException if no test exists with the given ID
     */
    void deleteTest(Long id);

    /**
     * Bulk uploads tests from a file (CSV or Excel format).
     * Processes multiple test records from a structured file and persists them.
     * Supports both CSV and Excel file formats.
     *
     * @param file The multipart file containing test data in supported format
     * @throws IllegalArgumentException if file is empty, invalid, or unsupported format
     * @throws RuntimeException if file processing or data persistence fails
     */
    TestServiceImpl.BulkUploadResult bulkUploadTests(MultipartFile file);

    /**
     * Updates an existing test with new data.
     * Finds the test by ID, applies updates from the DTO, and persists changes.
     *
     * @param id The unique ID of the test to update
     * @param test Data Transfer Object containing updated test information
     * @return TestDto The updated test data
     * @throws IllegalArgumentException if no test exists with the given ID
     */
    TestDto editTest(Long id, TestDto test);

    Page<TestDto> getAllTests(Pageable pageable);

    Page<TestDto> searchTests(String keyword, Pageable pageable);

    Page<TestDto> getTestsByStatus(Boolean published, Pageable pageable);

    Page<TestDto> getTestsByDurationRange(Integer minDuration, Integer maxDuration, Pageable pageable);

    Page<TestDto> findByAdvancedSearch(String keyword, Boolean published, Integer minDuration, Integer maxDuration, Pageable pageable);


    /**
     * Validates if a user can access a specific test based on multiple criteria.
     *
     * Performs comprehensive access validation including:
     * - Publication status check
     * - Scheduled test window validation (with buffers)
     * - IP address restrictions
     * - Secure browser requirements
     * - Maximum attempts enforcement
     *
     * This method is typically called when a student attempts to start a test
     * or when displaying test availability information.
     *
     * @param testId The ID of the test to validate
     * @param userIP The IP address of the user attempting access
     * @return TestAccessValidation result with access status and detailed message
     * @throws IllegalArgumentException if no test exists with the given ID
     *
     * Validation Steps:
     * 1. Check if test exists and is published
     * 2. Verify current time is within test window (considering buffers)
     * 3. Validate IP address against allowed IPs
     * 4. Check secure browser requirements (if applicable)
     * 5. Verify attempt limits (if configured)
     *
     * Example Usage:
     * {@code
     * TestAccessValidation validation = testService.validateTestAccess(1L, "192.168.1.100");
     * if (validation.isAccessible()) {
     *     // Allow test access
     * } else {
     *     // Show error message: validation.getMessage()
     * }
     * }
     */
    TestServiceImpl.TestAccessValidation validateTestAccess(Long testId, String userIP);

    /**
     * Retrieves test access information for display purposes.
     *
     * This method does not perform validation, but returns current status
     * information about the test's accessibility. Useful for displaying
     * test information to students before they attempt to start.
     *
     * @param testId The ID of the test
     * @return TestAccessInfo containing current access status and details
     * @throws IllegalArgumentException if no test exists with the given ID
     *
     * Returned Information:
     * - Current test status (DRAFT, SCHEDULED, ACTIVE, EXPIRED)
     * - Accessibility flag
     * - Scheduled start and end times
     * - IP restriction status
     * - Secure browser requirements
     * - Time remaining (for active tests)
     *
     * Example Usage:
     * {@code
     * TestAccessInfo accessInfo = testService.getTestAccessInfo(1L);
     * // Display: "Test starts in 2 hours" or "Test active - 45m remaining"
     * }
     */
    TestServiceImpl.TestAccessInfo getTestAccessInfo(Long testId);

    /*
     * Potential additional methods that could be added:

     * // Search tests by title or description
     * List<TestDto> searchTests(String keyword);

     * // Get tests by publication status
     * List<TestDto> getTestsByPublishedStatus(boolean published);

     * // Get tests with pagination support
     * Page<TestDto> getTests(Pageable pageable);

     * // Publish/unpublish a test
     * TestDto publishTest(Long id, boolean publish);

     * // Get test statistics
     * TestStatisticsDto getTestStatistics(Long id);

     * // Duplicate a test
     * TestDto duplicateTest(Long id);
     */
}