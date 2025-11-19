// Package declaration - organizes classes and prevents naming conflicts
package com.almubaraksuleiman.cbts.examiner.service.impl;

// Import statements - bring in required classes and dependencies

import com.almubaraksuleiman.cbts.dto.QuestionDto;
import com.almubaraksuleiman.cbts.examiner.model.*;
import com.almubaraksuleiman.cbts.examiner.repository.QuestionRepository;
import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
import com.almubaraksuleiman.cbts.examiner.service.QuestionService;
import com.almubaraksuleiman.cbts.mapper.QuestionMapper;
import com.almubaraksuleiman.cbts.resource.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Question-related operations.
 * This class handles business logic for creating, reading, updating, deleting,
 * and bulk uploading questions for tests.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Slf4j
@Service // Marks this class as a Spring service bean for dependency injection
@RequiredArgsConstructor // Lombok annotation to generate constructor for final fields
public class QuestionServiceImpl implements QuestionService {

    // Dependency injection of required repositories and mapper
    private final QuestionRepository questionRepo; // Handles database operations for questions
    private final TestRepository testRepository;   // Handles database operations for tests
    private final QuestionMapper questionMapper;   // Converts between DTO and Entity objects
    private final FileStorageService fileStorageService; // Service for handling file uploads

    /**
     * Adds a new question to a specific test.
     * Validates test existence, converts DTO to entity, sets association, and saves.
     *
     * @param testId The ID of the test to add the question to
     * @param dto    The question data transfer object containing question details
     * @return QuestionDto The saved question as a DTO
     * @throws IllegalArgumentException if test with given ID is not found
     */
    @Override
    public QuestionDto addQuestionToTest(Long testId, QuestionDto dto) {
        // Find the test by ID or throw exception if not found
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with id: " + testId));

        // Convert DTO to Entity using mapper
        Question question = questionMapper.toEntity(dto);

        // Establish the relationship between question and test
        question.setTest(test);

        // Save the question to database and get the persisted entity
        Question saved = questionRepo.save(question);

        // Convert the saved entity back to DTO and return
        return questionMapper.toDto(saved);
    }



    /**
     * Adds a new question to a specific test with media upload support.
     * Supports both URL-based media and file uploads to various storage backends.
     *
     * @param testId The ID of the test to add the question to
     * @param questionDto The question data transfer object containing question details
     * @param mediaFile Optional media file for upload (can be null for URL-based media)
     * @return QuestionDto The saved question as a DTO
     * @throws IllegalArgumentException if test not found or invalid question data
     */
//    @Transactional
//    @Override
//    public QuestionDto addQuestionToTest(Long testId, QuestionDto questionDto, MultipartFile mediaFile) {
//        log.info("Adding question to test ID: {}", testId);
//
//        // Validate test existence
//        Test test = testRepository.findById(testId)
//                .orElseThrow(() -> new IllegalArgumentException("Test not found with id: " + testId));
//
//        // Handle media file upload if provided
//        if (mediaFile != null && !mediaFile.isEmpty()) {
//            String mediaPath = fileStorageService.storeFile(mediaFile);
//            questionDto.setMediaPath(mediaPath);
//            questionDto.setMediaFileName(mediaFile.getOriginalFilename());
//            questionDto.setMediaFileSize(mediaFile.getSize());
//            log.debug("Media file uploaded: {}", mediaPath);
//        }
//
//        // Validate question configuration
//        if (!questionDto.isValidConfiguration()) {
//            throw new IllegalArgumentException("Invalid question configuration for type: " + questionDto.getType());
//        }
//
//        // Convert and save question
//        Question question = questionMapper.toEntity(questionDto);
//        question.setTest(test);
//
//        Question saved = questionRepo.save(question);
//        log.info("Question created successfully with ID: {}", saved.getId());
//
//        return questionMapper.toDto(saved);
//    }




    @Override
    @Transactional
    public QuestionDto addQuestionToTest(Long testId, QuestionDto questionDto, MultipartFile mediaFile) {
        log.info("Adding question to test ID: {}", testId);

        // Validate test existence
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with id: " + testId));

        // Handle media file upload if provided
        if (mediaFile != null && !mediaFile.isEmpty()) {
            try {
                String mediaPath = fileStorageService.storeFile(mediaFile);
                questionDto.setMediaPath(mediaPath);
                log.debug("Media file uploaded successfully: {}", mediaPath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload media file: " + e.getMessage(), e);
            }
        }

        // Validate question configuration
        if (!isValidQuestionConfiguration(questionDto)) {
            throw new IllegalArgumentException("Invalid question configuration for type: " + questionDto.getType());
        }

        // Convert and save question
        Question question = questionMapper.toEntity(questionDto);
        question.setTest(test);

        Question saved = questionRepo.save(question);
        log.info("Question created successfully with ID: {}", saved.getId());

        return questionMapper.toDto(saved);
    }

    /**
     * Updates an existing question with new data.
     * Finds the question, updates its fields, and persists changes.
     *
     * @param id          The ID of the question to update
     * @param questionDto The updated question data
     * @return QuestionDto The updated question as DTO
     * @throws IllegalArgumentException if question with given ID is not found
     */
    @Override
    public QuestionDto updateQuestion(Long id, QuestionDto questionDto) {
        // Find existing question or throw exception
        Question existing = questionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));

//        // Update question fields from the DTO
//        existing.setText(questionDto.getText());
//        existing.setChoices(questionDto.getChoices());
//        existing.setCorrectAnswer(questionDto.getCorrectAnswer());
//        existing.setType(questionDto.getType());
//        // Note: Test association update would go here if needed
//
//        // Save the updated question
//        Question updated = questionRepo.save(existing);
//        // Convert to DTO and return
//        return questionMapper.toDto(updated);

        //Update base question fields
        existing.setText(questionDto.getText());
        existing.setType(questionDto.getType());
        existing.setChoices(questionDto.getChoices());
        existing.setCorrectAnswer(questionDto.getCorrectAnswer());
        existing.setMaxMarks(questionDto.getMaxMarks());


        Question saved = questionRepo.save(existing);
        return questionMapper.toDto(saved);
    }




    /**
     * Updates an existing question with new data and optional media changes.
     * Supports media replacement, removal, or URL updates.
     *
     * @param id The ID of the question to update
     * @param questionDto The updated question data
     * @param mediaFile Optional new media file (null keeps existing, empty removes)
     * @return QuestionDto The updated question as DTO
     * @throws IllegalArgumentException if question not found
     */
//    @Transactional
//    @Override
//    public QuestionDto updateQuestion(Long id, QuestionDto questionDto, MultipartFile mediaFile) {
//        log.info("Updating question ID: {}", id);
//
//        Question existing = questionRepo.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));
//
//        // Handle media file changes
//        handleMediaUpdate(existing, questionDto, mediaFile);
//
//        // Update question fields
//        questionMapper.updateEntityFromDto(existing, questionDto);
//
//        Question saved = questionRepo.save(existing);
//        log.info("Question updated successfully with ID: {}", saved.getId());
//
//        return questionMapper.toDto(saved);
//    }


    @Override
    @Transactional
    public QuestionDto updateQuestion(Long id, QuestionDto questionDto, MultipartFile mediaFile) {
        log.info("Updating question ID: {}", id);

        Question existing = questionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));

        // Handle media file changes
        handleMediaUpdate(existing, questionDto, mediaFile);

        // Update question fields
        questionMapper.updateEntityFromDto(existing, questionDto);

        Question saved = questionRepo.save(existing);
        log.info("Question updated successfully with ID: {}", saved.getId());

        return questionMapper.toDto(saved);
    }




    /**
     * Handles media file updates including upload, replacement, and deletion.
     *
     * @param existing The existing question entity
     * @param questionDto The updated question DTO
     * @param mediaFile The new media file (can be null)
     */
//    private void handleMediaUpdate(Question existing, QuestionDto questionDto, MultipartFile mediaFile) {
//        // Case 1: New file uploaded
//        if (mediaFile != null && !mediaFile.isEmpty()) {
//            String newMediaPath = fileStorageService.storeFile(mediaFile);
//
//            // Delete old media file if it exists
//            if (existing.getMediaPath() != null && !existing.getMediaPath().isEmpty()) {
//                fileStorageService.deleteFile(existing.getMediaPath());
//            }
//
//            questionDto.setMediaPath(newMediaPath);
//            questionDto.setMediaFileName(mediaFile.getOriginalFilename());
//            questionDto.setMediaFileSize(mediaFile.getSize());
//        }
//        // Case 2: Media removed (mediaType set to NONE)
//        else if (questionDto.getMediaType() == null || questionDto.getMediaType() == MediaType.NONE) {
//            if (existing.getMediaPath() != null && !existing.getMediaPath().isEmpty()) {
//                fileStorageService.deleteFile(existing.getMediaPath());
//            }
//            questionDto.setMediaPath(null);
//            questionDto.setMediaCaption(null);
//        }
//        // Case 3: URL-based media updated
//        else if (questionDto.getMediaPath() != null && !questionDto.getMediaPath().equals(existing.getMediaPath())) {
//            // If it's a file path (not URL), delete the old file
//            if (existing.getMediaPath() != null && !existing.getMediaPath().startsWith("http")) {
//                fileStorageService.deleteFile(existing.getMediaPath());
//            }
//        }
//    }



    /**
     * Handles media file updates including upload, replacement, and deletion.
     */
    private void handleMediaUpdate(Question existing, QuestionDto questionDto, MultipartFile mediaFile) {
        // Case 1: New file uploaded
        if (mediaFile != null && !mediaFile.isEmpty()) {
            try {
                String newMediaPath = fileStorageService.storeFile(mediaFile);

                // Delete old media file if it exists and was stored locally
                if (existing.getMediaPath() != null && !existing.getMediaPath().isEmpty() &&
                        !existing.getMediaPath().startsWith("http")) {
                    fileStorageService.deleteFile(existing.getMediaPath());
                }

                questionDto.setMediaPath(newMediaPath);
                log.debug("Media file updated: {}", newMediaPath);

            } catch (Exception e) {
                throw new RuntimeException("Failed to update media file: " + e.getMessage(), e);
            }
        }
        // Case 2: Media removed (mediaType set to NONE)
        else if (questionDto.getMediaType() == null || questionDto.getMediaType() == MediaType.NONE) {
            if (existing.getMediaPath() != null && !existing.getMediaPath().isEmpty() &&
                    !existing.getMediaPath().startsWith("http")) {
                fileStorageService.deleteFile(existing.getMediaPath());
            }
            questionDto.setMediaPath(null);
            questionDto.setMediaCaption(null);
            log.debug("Media file removed");
        }
        // Case 3: URL-based media updated
        else if (questionDto.getMediaPath() != null && !questionDto.getMediaPath().equals(existing.getMediaPath())) {
            // If it's a file path (not URL), delete the old file
            if (existing.getMediaPath() != null && !existing.getMediaPath().startsWith("http")) {
                fileStorageService.deleteFile(existing.getMediaPath());
            }
            log.debug("Media URL updated: {}", questionDto.getMediaPath());
        }
    }



    /**
     * Validates question configuration consistency.
     */
    private boolean isValidQuestionConfiguration(QuestionDto questionDto) {
        if (questionDto.getText() == null || questionDto.getText().trim().isEmpty()) {
            return false;
        }

        if (questionDto.getType() != null) {
            switch (questionDto.getType()) {
                case MULTIPLE_CHOICE:
                case MULTIPLE_SELECT:
                    return questionDto.getChoices() != null && !questionDto.getChoices().trim().isEmpty() &&
                            questionDto.getCorrectAnswer() != null && !questionDto.getCorrectAnswer().trim().isEmpty();
                case TRUE_FALSE:
                    return "True".equals(questionDto.getCorrectAnswer()) || "False".equals(questionDto.getCorrectAnswer());
                case FILL_IN_THE_BLANK:
                case ESSAY:
                    return questionDto.getCorrectAnswer() != null && !questionDto.getCorrectAnswer().trim().isEmpty();
                case MATCHING:
                    return questionDto.getChoices() != null && !questionDto.getChoices().trim().isEmpty();
                default:
                    return true;
            }
        }
        return false;
    }



    /**
     * Retrieves a specific question by its ID.
     *
     * @param questionId The ID of the question to retrieve
     * @return QuestionDto The question data
     * @throws IllegalArgumentException if question with given ID is not found
     */
    @Override
    public QuestionDto getQuestionById(Long questionId) {
        // Find question by ID or throw exception
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));
        // Convert to DTO and return
        return questionMapper.toDto(question);
    }

    /**
     * Retrieves all questions belonging to a specific test.
     *
     * @param testId The ID of the test whose questions to retrieve
     * @return List<QuestionDto> List of questions as DTOs
     */
    @Override
    public List<QuestionDto> getQuestionsByTest(Long testId) {
        // Find all questions for the test, convert each to DTO, and collect to list
        return questionRepo.findByTestId(testId)
                .stream() // Convert list to stream for functional operations
                .map(questionMapper::toDto) // Convert each entity to DTO
                .collect(Collectors.toList()); // Collect results into a list
    }

    /**
     * Deletes a question by its ID.
     * Validates existence first to provide meaningful error messages.
     *
     * @param id The ID of the question to delete
     * @throws IllegalArgumentException if question with given ID is not found
     */
    @Override
    public void deleteQuestion(Long id) {
        // Check if question exists before attempting deletion
        if (!questionRepo.existsById(id)) {
            throw new IllegalArgumentException("Question not found with id: " + id);
        }
        // Delete the question from database
        questionRepo.deleteById(id);
    }

    /**
     * Bulk uploads questions from a file (CSV or Excel) to a specific test.
     * Supports both Excel (.xlsx) and CSV file formats.
     * Wrapped in transaction to ensure all-or-nothing operation.
     *
     * @param testId The ID of the test to add questions to
     * @param file   The uploaded file containing questions
     * @throws IllegalArgumentException if file is empty or format is unsupported
     * @throws RuntimeException         if file processing fails
     */
    @Override
    @Transactional // Ensures all database operations succeed or fail together
    public void bulkUploadQuestions(Long testId, MultipartFile file) {
        // Validate that file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Get filename and determine processing method based on extension
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.endsWith(".xlsx")) {
            // Process as Excel file
            readExcelFile(testId, file);
        } else if (fileName != null && (fileName.endsWith(".csv") || fileName.endsWith(".txt"))) {
            // Process as CSV or text file
            readCsvFile(testId, file);
        } else {
            // Throw error for unsupported formats
            throw new IllegalArgumentException("Unsupported file format. Please use CSV or Excel.");
        }
    }

    /**
     * Processes a CSV file to extract and save questions for a specific test.
     * Skips the header row and processes each subsequent line.
     *
     * @param testId The ID of the test to add questions to
     * @param file   The CSV file containing questions
     * @throws RuntimeException if test not found or file reading fails
     */
    private void readCsvFile(Long testId, MultipartFile file) {
        // Use try-with-resource to automatically close the reader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true; // Flag to skip header row

            // Find the test once to avoid multiple database calls
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found"));

            // Read file line by line
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header row
                    continue;
                }

                // Parse the CSV line into individual values
                List<String> data = parseCsvLine(line);

                // Skip lines that don't have enough data (at least text, choices, answer)
                if (data.size() < 3) continue;

                // Build question using builder pattern
                Question q = Question.builder()
                        .text(data.get(0))                    // Question text
                        .choices(data.get(1))                 // Answer choices
                        .correctAnswer(data.get(2))           // Correct answer
                        .type(data.size() > 3 ?               // Question type (default to MULTIPLE_CHOICE)
                                QuestionType.valueOf(data.get(3).toUpperCase()) :
                                QuestionType.MULTIPLE_CHOICE)
                        .maxMarks(data.size() > 4 ?           // Maximum marks (default to 1.0)
                                Double.valueOf(data.get(4)) : 1.0)
                        .test(test)                          // Associate with test
                        .build();

                // Save the question to database
                questionRepo.save(q);
            }
        } catch (IOException e) {
            // Wrap and rethrow with descriptive message
            throw new RuntimeException("CSV upload failed", e);
        }
    }

    /**
     * Parses a single line from a CSV file, handling quoted values and commas within quotes.
     * This method properly handles CSV fields that may contain commas and are enclosed in quotes.
     *
     * @param line A single line from the CSV file
     * @return List of parsed values from the CSV line
     */
    private List<String> parseCsvLine(String line) {
        // Initialize an empty list to store the parsed values from the CSV line
        List<String> values = new ArrayList<>();

        // Flag to track whether we're currently inside a quoted section
        // This helps handle commas that are part of the data, not separators
        boolean inQuotes = false;

        // Temporary builder to construct each value character by character
        StringBuilder currentValue = new StringBuilder();

        // Iterate through each character in the CSV line
        for (char c : line.toCharArray()) {
            // Check if the current character is a double quote
            if (c == '"') {
                // Toggle the inQuotes flag - if we were in quotes, now we're out, and vice versa
                // This handles both opening and closing quotes
                inQuotes = !inQuotes;
            }
            // Check if the current character is a comma AND we're NOT inside quotes
            else if (c == ',' && !inQuotes) {
                // We've reached the end of a field (comma outside quotes = field separator)
                // Add the completed value to our list after trimming whitespace
                values.add(currentValue.toString().trim());

                // Reset the StringBuilder to start building the next value
                currentValue = new StringBuilder();
            }
            // For all other characters (including commas inside quotes)
            else {
                // Append the character to the current value being built
                // This includes commas that are inside quoted sections
                currentValue.append(c);
            }
        }

        // After processing all characters, add the final value to the list
        // This handles the last field in the line (which doesn't end with a comma)
        values.add(currentValue.toString().trim());

        // Return the complete list of parsed values
        return values;
    }

    /**
     * Processes an Excel file to extract and save questions for a specific test.
     * Reads the first sheet, skips the header row, and processes each data row.
     *
     * @param testId The ID of the test to add questions to
     * @param file   The Excel file containing questions
     * @throws RuntimeException if test not found or file processing fails
     */
    private void readExcelFile(Long testId, MultipartFile file) {
        // Use try-with-resource to automatically close input stream and workbook
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            // Get the first sheet from the workbook
            Sheet sheet = workbook.getSheetAt(0);

            // Process each row starting from index 1 (skip header row at index 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue; // Skip empty rows

                // Extract values from each cell with null safety
                String text = row.getCell(0).getStringCellValue();
                String choices = row.getCell(1).getStringCellValue();
                String correctAnswer = row.getCell(2).getStringCellValue();

                // Handle optional fields with default values
                String type = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : "MULTIPLE_CHOICE";
                Double maxMarks = row.getCell(4) != null ? row.getCell(4).getNumericCellValue() : 1.0;

                // Create question object
                Question q = new Question();
                q.setText(text);
                q.setChoices(choices);
                q.setCorrectAnswer(correctAnswer);
                q.setType(QuestionType.valueOf(type.toUpperCase())); // Convert string to enum
                q.setMaxMarks(maxMarks);

                // Associate with test
                Test test = testRepository.findById(testId)
                        .orElseThrow(() -> new RuntimeException("Test not found"));
                q.setTest(test);

                // Save question to database
                questionRepo.save(q);
            }
        } catch (IOException e) {
            // Wrap and rethrow with descriptive message
            throw new RuntimeException("Excel upload failed", e);
        }
    }


    /**
     * Get questions by test with pagination, sorting, and filtering
     */
    @Override
    public Page<QuestionDto> getQuestionsByTest(Long testId, Pageable pageable) {
        Page<Question> questionsPage = questionRepo.findByTestId(testId, pageable);
        return questionsPage.map(questionMapper::toDto);
    }

    /**
     * Search questions by keyword with pagination within a test
     */
    @Override
    public Page<QuestionDto> searchQuestions(Long testId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getQuestionsByTest(testId, pageable);
        }

        Page<Question> questionsPage = questionRepo.findByTestIdAndTextContainingIgnoreCaseOrChoicesContainingIgnoreCaseOrCorrectAnswerContainingIgnoreCase(
                testId, keyword, keyword, keyword, pageable);
        return questionsPage.map(questionMapper::toDto);
    }

    /**
     * Get questions by type with pagination within a test
     */
    @Override
    public Page<QuestionDto> getQuestionsByType(Long testId, String type, Pageable pageable) {
        Page<Question> questionsPage = questionRepo.findByTestIdAndType(testId, type, pageable);
        return questionsPage.map(questionMapper::toDto);
    }

    /**
     * Get questions by marks range with pagination within a test
     */
    @Override
    public Page<QuestionDto> getQuestionsByMarksRange(Long testId, Double minMarks, Double maxMarks, Pageable pageable) {
        if (minMarks == null) minMarks = 0.0;
        if (maxMarks == null) maxMarks = Double.MAX_VALUE;

        Page<Question> questionsPage = questionRepo.findByTestIdAndMaxMarksBetween(testId, minMarks, maxMarks, pageable);
        return questionsPage.map(questionMapper::toDto);
    }

    /**
     * Advanced search with multiple criteria within a test
     */
    @Override
    public Page<QuestionDto> findByAdvancedSearch(Long testId, String keyword, String type, Double minMarks, Double maxMarks, Pageable pageable) {
        Page<Question> questionsPage = questionRepo.findByAdvancedSearch(testId, keyword, type, minMarks, maxMarks, pageable);
        return questionsPage.map(questionMapper::toDto);
    }


}