// Package declaration - organizes classes within the service implementation package
package com.almubaraksuleiman.cbts.examiner.service.impl;

// Import statements - required dependencies for the class
import com.almubaraksuleiman.cbts.dto.TestDto;
import com.almubaraksuleiman.cbts.examiner.SecurityUtils;
import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.examiner.repository.ExaminerRepository;
import com.almubaraksuleiman.cbts.mapper.TestMapper;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
import com.almubaraksuleiman.cbts.examiner.service.TestService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Test-related operations.
 * Handles business logic for creating, reading, updating, deleting,
 * and bulk uploading tests in the CBTS system.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Slf4j
@Service // Marks this class as a Spring service bean for dependency injection
@AllArgsConstructor // Lombok annotation to generate constructor with all fields
public class TestServiceImpl implements TestService {

    // Repository for database operations on Test entities
    private final TestRepository testRepository;

    private final ExaminerRepository examinerRepository;

    // Mapper for converting between Test DTO and Entity objects
    @Autowired // Explicit autowiring (redundant with @AllArgsConstructor but explicit)
    private final TestMapper testMapper;

    private final SecurityUtils securityUtils;

    /**
     * Creates a new test from the provided DTO.
     * Converts DTO to entity, saves to database, and returns the saved entity as DTO.
     *
     * @param testDto Data Transfer Object containing test information
     * @return TestDto The created test as DTO
     */
    @Override
    public TestDto createTest(TestDto testDto) {
        securityUtils.validateExaminerOrAdmin();

        Examiner examiner = securityUtils.getCurrentExaminer();
        Test test = testMapper.toEntity(testDto);
        test.setCreatedBy(examiner);

        Test saved = testRepository.save(test);
        return testMapper.toDto(saved);
    }


    /**
     * Updates an existing test with new data.
     * Finds the test by ID, updates all fields from DTO, and saves changes.
     *
     * @param id The ID of the test to update
     * @param testDto DTO containing updated test data
     * @return TestDto The updated test as DTO
     * @throws IllegalArgumentException if test with given ID is not found
     */
    @Override
    public TestDto editTest(Long id, TestDto testDto) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));

        securityUtils.validateTestAccess(test);

        // Update fields
        test.setTitle(testDto.getTitle());
        test.setDescription(testDto.getDescription());
        test.setDurationMinutes(testDto.getDurationMinutes());
        test.setNumberOfQuestions(testDto.getNumberOfQuestions());
        test.setRandomizeQuestions(testDto.getRandomizeQuestions());
        test.setShuffleChoices(testDto.getShuffleChoices());
        test.setPublished(testDto.getPublished());
        test.setPassingScore(testDto.getPassingScore());
        test.setTotalMarks(testDto.getTotalMarks());

        return testMapper.toDto(testRepository.save(test));
    }

    /**
     * Retrieves all tests from the database.
     * Converts each test entity to DTO for response.
     *
     * @return List<TestDto> List of all tests as DTOs
     */
    @Override
    public List<TestDto> getAllTests() {
        securityUtils.validateExaminerOrAdmin();

        List<Test> tests;
        if (securityUtils.isAdmin()) {
            tests = testRepository.findAll();
        } else {
            Examiner examiner = securityUtils.getCurrentExaminer();
            tests = testRepository.findByCreatedBy(examiner, Pageable.unpaged()).getContent();
        }

        return tests.stream()
                .map(testMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific test by its ID.
     *
     * @param id The ID of the test to retrieve
     * @return TestDto The test data as DTO
     * @throws IllegalArgumentException if test with given ID is not found
     */
    @Override
    public TestDto getTestById(Long id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with id: " + id));

        securityUtils.validateTestAccess(test);

        return testMapper.toDto(test);
    }

    /**
     * Deletes a test by its ID.
     * Uses Spring Data JPA's deleteById method.
     *
     * @param id The ID of the test to delete
     */
    @Override
    public void deleteTest(Long id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));

        securityUtils.validateTestAccess(test);

        testRepository.delete(test);
    }

    /**
     * Bulk uploads tests from a file (CSV or Excel) with enhanced validation.
     * Determines file format and delegates to appropriate processing method.
     *
     * @param file The uploaded file containing test data
     * @return BulkUploadResult Summary of upload operation
     * @throws RuntimeException if filename is missing, format is unsupported, or upload fails
     */
    @Override
    public BulkUploadResult bulkUploadTests(MultipartFile file) {
        securityUtils.validateExaminerOrAdmin();

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or null");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new RuntimeException("File name is missing");
        }

        try {
            String lowerName = fileName.toLowerCase();
            List<Test> uploadedTests;

            if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
                uploadedTests = importFromExcel(file);
            } else if (lowerName.endsWith(".csv")) {
                uploadedTests = importFromCsv(file);
            } else {
                throw new RuntimeException("Unsupported file format. Please upload CSV or Excel.");
            }

            // Validate we have tests to upload
            if (uploadedTests.isEmpty()) {
                throw new RuntimeException("No valid test data found in the file");
            }

            // Set creator for all tests if examiner
            Examiner creator = null;
            if (!securityUtils.isAdmin()) {
                creator = securityUtils.getCurrentExaminer();
                for (Test test : uploadedTests) {
                    test.setCreatedBy(creator);
                    // Set additional default values if needed
                    if (test.getPublished() == null) {
                        test.setPublished(false); // Default to unpublished
                    }
                }
            }

            // Save all tests
            List<Test> savedTests = testRepository.saveAll(uploadedTests);

            return BulkUploadResult.builder()
                    .totalProcessed(uploadedTests.size())
                    .successfullyUploaded(savedTests.size())
                    .message(String.format("Successfully uploaded %d tests", savedTests.size()))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Bulk upload failed: " + e.getMessage(), e);
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BulkUploadResult {
        private int totalProcessed;
        private int successfullyUploaded;
        private String message;
        private LocalDateTime timestamp;

        public BulkUploadResult(int totalProcessed, int successfullyUploaded, String message) {
            this.totalProcessed = totalProcessed;
            this.successfullyUploaded = successfullyUploaded;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }


    /**
     * Processes a CSV file to import multiple tests.
     * Reads file line by line, skips header, and parses each data row.
     *
     * @param file The CSV file containing test data
     * @throws RuntimeException if file reading fails
     *
     * Processes a CSV file to import multiple tests with enhanced validation.
     */
    private List<Test> importFromCsv(MultipartFile file) throws IOException {
        List<Test> tests = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                try {
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    // Basic validation - at least title should be present
                    String title = safeString(data, 0);
                    if (title == null || title.trim().isEmpty()) {
                        System.err.println("Skipping line " + lineNumber + ": Missing title");
                        continue;
                    }

                    Test test = Test.builder()
                            .title(title)
                            .description(safeString(data, 1))
                            .durationMinutes(safeInt(data, 2, 60)) // Default 60 minutes
                            .shuffleChoices(safeBoolean(data, 3))
                            .numberOfQuestions(safeInt(data, 4, 0))
                            .published(safeBoolean(data, 5))
                            .totalMarks(safeInt(data, 6, 100)) // Default 100 marks
                            .randomizeQuestions(safeBoolean(data, 7))
                            .passingScore(safeInt(data, 8, 50)) // Default 50% passing
                            .build();

                    tests.add(test);

                } catch (Exception e) {
                    System.err.println("Error processing line " + lineNumber + ": " + e.getMessage());
                    // Continue processing other lines instead of failing entire upload
                }
            }
        } catch (IOException e) {
            throw new IOException("CSV upload failed at line " + lineNumber, e);
        }

        return tests;
    }


    /**
     * Safely extracts a string value from an array at given index.
     * Removes surrounding quotes and trims whitespace.
     *
     * @param arr The string array to extract from
     * @param index The index to extract
     * @return String The cleaned string value, or null if index out of bounds
     */
    private String safeString(String[] arr, int index) {
        return index < arr.length ? arr[index].replaceAll("^\"|\"$", "").trim() : null;
    }

    /**
     * Safely extracts an integer value from an array at given index.
     * Returns 0 if parsing fails or index out of bounds.
     *
     * @param arr The string array to extract from
     * @param index The index to extract
     * @return int The parsed integer value, or 0 on error
     */
    private int safeInt(String[] arr, int index, int defaultValue) {
        try {
            String value = safeString(arr, index);
            return value != null && !value.trim().isEmpty() ?
                    Integer.parseInt(value.trim()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Safely extracts a boolean value from an array at given index.
     * Supports multiple truthy values: "true", "yes", "1".
     *
     * @param arr The string array to extract from
     * @param index The index to extract
     * @return boolean The parsed boolean value, or false on error
     */
    private boolean safeBoolean(String[] arr, int index) {
        String val = safeString(arr, index);
        if (val == null) return false;

        val = val.toLowerCase().trim();
        return val.equals("true") || val.equals("yes") || val.equals("1") ||
                val.equals("y") || val.equals("t");
    }

    /**
     * Processes an Excel file to import multiple tests.
     * Reads the first sheet, skips header, and processes each data row.
     *
     * @param file The Excel file containing test data
     * @throws RuntimeException if file processing fails

     * Processes an Excel file to import multiple tests with better error handling.
     */
    private List<Test> importFromExcel(MultipartFile file) throws Exception {
        List<Test> tests = new ArrayList<>();
        int rowNumber = 0;

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                rowNumber = i + 1; // +1 for header row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Check if row has any data
                    boolean hasData = false;
                    for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
                        if (getCellValueAsString(row.getCell(cellNum)) != null) {
                            hasData = true;
                            break;
                        }
                    }
                    if (!hasData) continue;

                    String title = getCellValueAsString(row.getCell(0));
                    if (title == null || title.trim().isEmpty()) {
                        System.err.println("Skipping row " + rowNumber + ": Missing title");
                        continue;
                    }

                    Test test = Test.builder()
                            .title(title)
                            .numberOfQuestions((int) getNumericCellValue(row.getCell(1)))
                            .randomizeQuestions(getBooleanCellValue(row.getCell(2)))
                            .shuffleChoices(getBooleanCellValue(row.getCell(3)))
                            .description(getCellValueAsString(row.getCell(4)))
                            .durationMinutes((int) getNumericCellValue(row.getCell(5)))
                            .totalMarks((int) getNumericCellValue(row.getCell(6)))
                            .published(getBooleanCellValue(row.getCell(7)))
                            .passingScore((int) getNumericCellValue(row.getCell(8)))
                            .build();

                    tests.add(test);

                } catch (Exception e) {
                    System.err.println("Error processing row " + rowNumber + ": " + e.getMessage());
                    // Continue processing other rows
                }
            }
            workbook.close();
        } catch (Exception e) {
            throw new Exception("Excel import failed at row " + rowNumber, e);
        }

        return tests;
    }


    // Reusable DataFormatter for consistent cell value formatting
    private final DataFormatter formatter = new DataFormatter();

    /**
     * Safely extracts string value from an Excel cell.
     * Handles null cells and trims whitespace.
     *
     * @param cell The Excel cell to extract value from
     * @return String The cell value as string, or null if cell is null
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Safely extracts numeric value from an Excel cell.
     * Returns 0 if cell is null or value cannot be parsed.
     *
     * @param cell The Excel cell to extract value from
     * @return double The numeric value, or 0 on error
     */
    private double getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        String value = formatter.formatCellValue(cell).trim();
        if (value.isEmpty()) return 0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0; // Return default value on parsing error
        }
    }

    /**
     * Safely extracts boolean value from an Excel cell.
     * Supports multiple truthy values: "true", "yes", "1".
     *
     * @param cell The Excel cell to extract value from
     * @return boolean The boolean value, or false on error
     */
    private boolean getBooleanCellValue(Cell cell) {
        if (cell == null) return false;
        String val = formatter.formatCellValue(cell).trim().toLowerCase();
        return val.equals("true") || val.equals("yes") || val.equals("1");
    }







    /**
     * Get all tests with pagination, sorting, and filtering
     */
    @Override
    public Page<TestDto> getAllTests(Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        Page<Test> testsPage;
        if (securityUtils.isAdmin()) {
            testsPage = testRepository.findAll(pageable);
        } else {
            Examiner examiner = securityUtils.getCurrentExaminer();
            testsPage = testRepository.findByCreatedBy(examiner, pageable);
        }

        return testsPage.map(testMapper::toDto);
    }


    /**
     * Search tests by keyword with pagination
     */
    @Override
    public Page<TestDto> searchTests(String keyword, Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTests(pageable);
        }

        Page<Test> testsPage;
        if (securityUtils.isAdmin()) {
            testsPage = testRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword, keyword, pageable);
        } else {
            Examiner examiner = securityUtils.getCurrentExaminer();
            testsPage = testRepository.findByCreatedByAndTitleContainingIgnoreCaseOrCreatedByAndDescriptionContainingIgnoreCase(
                    examiner, keyword, examiner, keyword, pageable);
        }

        return testsPage.map(testMapper::toDto);
    }



    /**
     * Get tests by published status with pagination
     */
    @Override
    public Page<TestDto> getTestsByStatus(Boolean published, Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        Page<Test> testsPage;
        if (securityUtils.isAdmin()) {
            testsPage = testRepository.findByPublished(published, pageable);
        } else {
            Examiner examiner = securityUtils.getCurrentExaminer();
            testsPage = testRepository.findByCreatedByAndPublished(examiner, published, pageable);
        }

        return testsPage.map(testMapper::toDto);
    }

    /**
     * Get tests by duration range with pagination
     */
    @Override
    public Page<TestDto> getTestsByDurationRange(Integer minDuration, Integer maxDuration, Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        if (minDuration == null) minDuration = 0;
        if (maxDuration == null) maxDuration = Integer.MAX_VALUE;

        Page<Test> testsPage;
        if (securityUtils.isAdmin()) {
            testsPage = testRepository.findByDurationMinutesBetween(minDuration, maxDuration, pageable);
        } else {
            Examiner examiner = securityUtils.getCurrentExaminer();
            testsPage = testRepository.findByCreatedByAndDurationMinutesBetween(
                    examiner, minDuration, maxDuration, pageable);
        }

        return testsPage.map(testMapper::toDto);
    }

    /**
     * Advanced search with multiple criteria
     */
    @Override
    public Page<TestDto> findByAdvancedSearch(String keyword, Boolean published,
                                              Integer minDuration, Integer maxDuration, Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        Page<Test> testsPage;
        if (securityUtils.isAdmin()) {
            testsPage = testRepository.findByAdvancedSearch(keyword, published, minDuration, maxDuration, pageable);
        } else {
            Examiner examiner = securityUtils.getCurrentExaminer();
            testsPage = testRepository.findByExaminerAndAdvancedSearch(
                    examiner, keyword, published, minDuration, maxDuration, pageable);
        }

        return testsPage.map(testMapper::toDto);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) return null;
        return auth.getAuthorities().iterator().next().getAuthority();
    }











    /**
     * Validates test access for current user based on test window configuration
     * This method is typically called when a student attempts to start a test
     *
     * @param testId The ID of the test to validate access for
     * @param userIP The IP address of the user attempting to access the test
     * @return TestAccessValidation containing access status and message
     * @throws IllegalArgumentException if test with given ID is not found
     */
    @Override
    public TestAccessValidation validateTestAccess(Long testId, String userIP) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with id: " + testId));

        return validateTestAccess(test, userIP);
    }

    /**
     * Comprehensive test access validation with multiple security checks:
     * 1. Publication status
     * 2. Scheduled test window (with buffers)
     * 3. IP address restrictions
     * 4. Secure browser requirements (would be handled at controller level)
     *
     * @param test The test entity to validate access for
     * @param userIP The IP address of the user attempting to access the test
     * @return TestAccessValidation containing access status and detailed message
     */
    public TestAccessValidation validateTestAccess(Test test, String userIP) {
        TestAccessValidation validation = new TestAccessValidation();

        // Check if test is published
        if (!Boolean.TRUE.equals(test.getPublished())) {
            validation.setAccessible(false);
            validation.setMessage("Test is not published");
            validation.setErrorCode("TEST_NOT_PUBLISHED");
            return validation;
        }

        // Check test window schedule
        if (!test.isCurrentlyAccessible()) {
            validation.setAccessible(false);

            LocalDateTime now = LocalDateTime.now();
            if (test.getScheduledStartTime() != null && now.isBefore(test.getScheduledStartTime())) {
                long minutesUntilStart = java.time.Duration.between(now, test.getScheduledStartTime()).toMinutes();
                validation.setMessage(String.format("Test is scheduled to start in %d minutes", minutesUntilStart));
                validation.setErrorCode("TEST_NOT_STARTED");
            } else if (test.getScheduledEndTime() != null && now.isAfter(test.getScheduledEndTime())) {
                validation.setMessage("Test has ended and is no longer available");
                validation.setErrorCode("TEST_EXPIRED");
            } else {
                validation.setMessage("Test is not currently available");
                validation.setErrorCode("TEST_UNAVAILABLE");
            }
            return validation;
        }

        // Check IP restrictions
        if (test.getAllowedIPs() != null && !test.getAllowedIPs().trim().isEmpty()) {
            if (!isIPAllowed(userIP, test.getAllowedIPs())) {
                validation.setAccessible(false);
                validation.setMessage("Access denied from your IP address. Please contact your administrator.");
                validation.setErrorCode("IP_RESTRICTED");
                return validation;
            }
        }

        // All checks passed - access granted
        validation.setAccessible(true);
        validation.setMessage("Access granted");
        validation.setErrorCode("ACCESS_GRANTED");

        // Calculate time remaining if test has end time
        if (test.getScheduledEndTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            long minutesRemaining = java.time.Duration.between(now, test.getScheduledEndTime()).toMinutes();
            validation.setTimeRemainingMinutes(minutesRemaining);
        }

        return validation;
    }

    /**
     * IP validation helper method with support for:
     * - Exact IP matches
     * - Wildcard patterns (192.168.*)
     * - CIDR notation (requires proper implementation)
     * - Range validation
     *
     * @param userIP The user's IP address to validate
     * @param allowedIPs Comma-separated list of allowed IP patterns
     * @return boolean indicating if IP is allowed
     */
    private boolean isIPAllowed(String userIP, String allowedIPs) {
        if (userIP == null || userIP.trim().isEmpty()) {
            return false; // Reject if no IP provided
        }

        String[] allowedRanges = allowedIPs.split(",");
        for (String range : allowedRanges) {
            range = range.trim();

            // Exact match
            if (range.equals(userIP)) {
                return true;
            }

            // Allow all IPs (0.0.0.0 or *)
            if (range.equals("0.0.0.0") || range.equals("*")) {
                return true;
            }

            // Wildcard pattern (e.g., 192.168.*)
            if (range.contains("*")) {
                String pattern = range.replace(".", "\\.").replace("*", ".*");
                if (userIP.matches(pattern)) {
                    return true;
                }
            }

            // CIDR notation (basic implementation - consider using a library like IPAddress)
            if (range.contains("/")) {
                if (isIPInCIDRRange(userIP, range)) {
                    return true;
                }
            }

            // IP range (e.g., 192.168.1.1-192.168.1.100)
            if (range.contains("-")) {
                if (isIPInRange(userIP, range)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Basic CIDR range validation (simplified - consider using a proper IP library)
     */
    private boolean isIPInCIDRRange(String userIP, String cidrRange) {
        try {
            String[] parts = cidrRange.split("/");
            String network = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            // This is a simplified implementation
            //I will be using  using a library like https://github.com/seancfoley/IPAddress
            String[] networkOctets = network.split("\\.");
            String[] userOctets = userIP.split("\\.");

            if (networkOctets.length != 4 || userOctets.length != 4) {
                return false;
            }

            // Simple prefix matching for demonstration
            // In production, use proper bitmask operations
            for (int i = 0; i < prefixLength / 8; i++) {
                if (!networkOctets[i].equals(userOctets[i])) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.warn("Invalid CIDR notation: {}", cidrRange);
            return false;
        }
    }

    /**
     * IP range validation (e.g., 192.168.1.1-192.168.1.100)
     */
    private boolean isIPInRange(String userIP, String ipRange) {
        try {
            String[] rangeParts = ipRange.split("-");
            if (rangeParts.length != 2) {
                return false;
            }

            String startIP = rangeParts[0].trim();
            String endIP = rangeParts[1].trim();

            long userIPLong = ipToLong(userIP);
            long startIPLong = ipToLong(startIP);
            long endIPLong = ipToLong(endIP);

            return userIPLong >= startIPLong && userIPLong <= endIPLong;
        } catch (Exception e) {
            log.warn("Invalid IP range notation: {}", ipRange);
            return false;
        }
    }

    /**
     * Convert IP address to long for range comparison
     */
    private long ipToLong(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result += Long.parseLong(octets[i]) << (24 - (8 * i));
        }
        return result;
    }

    /**
     * Get test access information without validation (for display purposes)
     */
    @Override
    public TestAccessInfo getTestAccessInfo(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with id: " + testId));

        return TestAccessInfo.builder()
                .testId(test.getId())
                .testTitle(test.getTitle())
                .currentStatus(test.getCurrentStatus())
                .currentlyAccessible(test.isCurrentlyAccessible())
                .scheduledStartTime(test.getScheduledStartTime())
                .scheduledEndTime(test.getScheduledEndTime())
                .hasIPRestrictions(test.getAllowedIPs() != null && !test.getAllowedIPs().trim().isEmpty())
                .requiresSecureBrowser(Boolean.TRUE.equals(test.getSecureBrowser()))
                .timeRemaining(calculateTimeRemaining(test))
                .build();
    }

    /**
     * Calculate time remaining for active tests
     */
    private String calculateTimeRemaining(Test test) {
        if (test.getScheduledEndTime() == null || !test.isCurrentlyAccessible()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(now, test.getScheduledEndTime());

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0) {
            return String.format("%dh %dm remaining", hours, minutes);
        } else {
            return String.format("%dm remaining", minutes);
        }
    }



    /**
     * Data Transfer Object for test access validation results.
     *
     * Provides comprehensive information about test access validation including
     * access status, user-friendly messages, error codes, and contextual information.
     * Used by clients to understand why access was granted or denied.
     */
    @Data
    @Builder
    @AllArgsConstructor // Add this to support builder with all fields
    @NoArgsConstructor  // Add this for default constructor
    public static class TestAccessValidation {
        /**
         * Indicates whether access to the test is granted.
         * True if all validation checks pass, false otherwise.
         */
        private boolean accessible;

        /**
         * Human-readable message explaining the validation result.
         * Provides clear feedback to users about access status.
         */
        private String message;

        /**
         * Machine-readable code for client-side processing.
         * Allows clients to handle different access scenarios programmatically.
         */
        private String errorCode;

        /**
         * Timestamp when the validation was performed.
         * Useful for auditing and time-sensitive operations.
         */
        @Builder.Default // This ensures the default value is used with builder
        private LocalDateTime validatedAt = LocalDateTime.now();

        /**
         * Remaining time in minutes for active tests.
         * Only populated when test has a scheduled end time and access is granted.
         * Helps clients display countdown timers.
         */
        private Long timeRemainingMinutes;

        // Remove the custom constructor as it conflicts with Lombok builder
        // The @Builder.Default annotation handles the default value for validatedAt
    }

    /**
     * DTO for test access information (non-validating, for display purposes)
     */
    @Data
    @Builder
    public static class TestAccessInfo {
        private Long testId;
        private String testTitle;
        private Test.TestStatus currentStatus;
        private boolean currentlyAccessible;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        private boolean hasIPRestrictions;
        private boolean requiresSecureBrowser;
        private String timeRemaining;
    }

}


