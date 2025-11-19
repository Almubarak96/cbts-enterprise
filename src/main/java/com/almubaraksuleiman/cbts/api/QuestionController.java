// Package declaration - organizes classes within the API package
package com.almubaraksuleiman.cbts.api;

// Import statements - required dependencies for the controller
import com.almubaraksuleiman.cbts.dto.QuestionDto;
import com.almubaraksuleiman.cbts.examiner.service.QuestionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing questions within tests.
 * Provides endpoints for CRUD operations, bulk upload, and template downloads.
 * Cross-origin requests allowed from Angular development server.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@Slf4j
@CrossOrigin("http://localhost:4200") // Allow requests from Angular dev server
@RestController // Marks this class as a REST controller with request mapping methods
@RequestMapping("/api/admin/tests/{testId}/questions") // Base path for all endpoints
@RequiredArgsConstructor // Lombok: generates constructor for final fields
public class QuestionController {

    // Injected service for question business logic
    private final QuestionService questionService;

    /**
     * Creates a new question within a specific test.
     *
     * @param testId ID of the test to add the question to
     * @param dto    Question data transfer object
     * @return QuestionDto The created question
     */
    @PostMapping("/new")
    public QuestionDto addQuestion(@PathVariable Long testId, @RequestBody QuestionDto dto) {
        return questionService.addQuestionToTest(testId, dto);
    }


    /**
     * ENHANCED: Creates a new question with optional media file upload.
     * Supports both JSON and multipart form data.
     *
     * @param testId The ID of the test to add the question to
     * @param questionDto The question data transfer object
     * @param mediaFile Optional media file for the question
     * @return ResponseEntity<QuestionDto> The created question with appropriate status
     */
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addQuestionWithMedia(
            @PathVariable Long testId,
            @RequestPart("question") @Valid QuestionDto questionDto,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile) {

        log.info("Creating new question for test ID: {} with media file: {}",
                testId, mediaFile != null ? mediaFile.getOriginalFilename() : "None");

        try {
            QuestionDto createdQuestion = questionService.addQuestionToTest(testId, questionDto, mediaFile);
            log.info("Question created successfully with ID: {}", createdQuestion.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for creating question: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating question for test {}: {}", testId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create question: " + e.getMessage()));
        }
    }

    /**
     * Updates an existing question within a test.
     * The testId in path maintains RESTful structure but isn't used directly.
     *
     * @param testId      ID of the test (for RESTful URL structure)
     * @param questionId  ID of the question to update
     * @param questionDto Updated question data
     * @return ResponseEntity<QuestionDto> The updated question with OK status
     */
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionDto> updateQuestion(
            @PathVariable Long testId,
            @PathVariable Long questionId,
            @RequestBody QuestionDto questionDto
    ) {
        return ResponseEntity.ok(questionService.updateQuestion(questionId, questionDto));
    }


    /**
     * ENHANCED: Updates an existing question with optional media file.
     *
     * @param testId The ID of the test
     * @param questionId The ID of the question to update
     * @param questionDto The updated question data
     * @param mediaFile Optional new media file
     * @return ResponseEntity<QuestionDto> The updated question
     */
    @PutMapping(value = "/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateQuestionWithMedia(
            @PathVariable Long testId,
            @PathVariable Long questionId,
            @RequestPart("question") @Valid QuestionDto questionDto,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile) {

        log.info("Updating question ID: {} for test ID: {}", questionId, testId);

        try {
            QuestionDto updatedQuestion = questionService.updateQuestion(questionId, questionDto, mediaFile);
            log.info("Question updated successfully: {}", questionId);

            return ResponseEntity.ok(updatedQuestion);

        } catch (IllegalArgumentException e) {
            log.warn("Question not found for update: {} for test: {}", questionId, testId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating question {} for test {}: {}", questionId, testId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update question: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a specific question by its ID within a test.
     * The testId maintains RESTful structure but isn't used directly.
     *
     * @param testId     ID of the test (for RESTful URL structure)
     * @param questionId ID of the question to retrieve
     * @return ResponseEntity<QuestionDto> The question with OK status
     */
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDto> getQuestionById(
            @PathVariable Long testId,
            @PathVariable Long questionId
    ) {
        return ResponseEntity.ok(questionService.getQuestionById(questionId));
    }

    /**
     * Retrieves all questions belonging to a specific test.
     *
     * @param testId ID of the test whose questions to retrieve
     * @return List<QuestionDto> List of all questions in the test
     */
//    @GetMapping("")
//    public List<QuestionDto> getByTest(@PathVariable Long testId) {
//        return questionService.getQuestionsByTest(testId);
//    }

    /**
     * Deletes a question by its ID within a test.
     * The testId maintains RESTful structure but isn't used directly.
     *
     * @param testId     ID of the test (for RESTful URL structure)
     * @param questionId ID of the question to delete
     * @return ResponseEntity<Void> No content response indicating successful deletion
     */
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long testId,
            @PathVariable Long questionId
    ) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

//    /**
//     * Bulk uploads questions from a file (CSV or Excel) to a specific test.
//     * Returns enhanced response with success message and count of uploaded questions.
//     *
//     * @param testId ID of the test to add questions to
//     * @param file   The uploaded file containing questions
//     * @return ResponseEntity<?> Success response with count or error details
//     */
//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadQuestions(@PathVariable Long testId, @RequestParam("file") MultipartFile file) {
//        try {
//            // Process the bulk upload
//            questionService.bulkUploadQuestions(testId, file);
//
//            // Return success response with message and count of questions
//            return ResponseEntity.ok(Map.of(
//                    "message", "Questions uploaded successfully!",
//                    "count", questionService.getQuestionsByTest(testId).size()
//            ));
//        } catch (Exception e) {
//            // Return error response with descriptive message
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
//        }
//    }



    /**
     * ENHANCED: Bulk uploads questions from a file (CSV or Excel) to a specific test.
     * Returns comprehensive response with success message and statistics.
     *
     * @param testId The ID of the test to add questions to
     * @param file The uploaded file containing questions
     * @return ResponseEntity with upload results or error details
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadQuestions(
            @PathVariable Long testId,
            @RequestParam("file") MultipartFile file) {

        log.info("Bulk uploading questions for test ID: {} with file: {}",
                testId, file.getOriginalFilename());

        try {
            // Get count before upload for statistics
            int countBefore = questionService.getQuestionsByTest(testId).size();

            // Process the bulk upload
            questionService.bulkUploadQuestions(testId, file);

            // Get count after upload for statistics
            int countAfter = questionService.getQuestionsByTest(testId).size();
            int uploadedCount = countAfter - countBefore;

            // Return comprehensive success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Questions uploaded successfully!");
            response.put("uploadedCount", uploadedCount);
            response.put("totalCount", countAfter);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());

            log.info("Bulk upload completed: {} questions uploaded", uploadedCount);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid bulk upload request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error during bulk upload for test {}: {}", testId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }




//    /**
//     * Downloads a question template file in either CSV or Excel format.
//     * Provides sample data to guide users on the required format.
//     *
//     * @param format   Desired file format ("csv" or "excel") - defaults to "csv"
//     * @param response HttpServletResponse to write the file to
//     * @throws IOException if file writing fails
//     */
//    @GetMapping("/download-question-template")
//    public void downloadQuestionTemplate(
//            @RequestParam(defaultValue = "csv") String format,
//            HttpServletResponse response) throws IOException {
//
//        // Define column headers for the template
//        String[] headers = {"Text", "Choices", "Correct Answer", "Type", "Max Marks"};
//
//        if ("excel".equalsIgnoreCase(format)) {
//            // Set response headers for Excel download
//            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            response.setHeader("Content-Disposition", "attachment; filename=question-template.xlsx");
//
//            // Create Excel workbook and sheet
//            Workbook workbook = new XSSFWorkbook();
//            Sheet sheet = workbook.createSheet("Questions");
//
//            // Create header row with column names
//            Row headerRow = sheet.createRow(0);
//            for (int i = 0; i < headers.length; i++) {
//                headerRow.createCell(i).setCellValue(headers[i]);
//            }
//
//            // Create example row with sample data
//            Row exampleRow = sheet.createRow(1);
//            exampleRow.createCell(0).setCellValue("What is 2+2?");
//            exampleRow.createCell(1).setCellValue("1,2,3,4");
//            exampleRow.createCell(2).setCellValue("4");
//            exampleRow.createCell(3).setCellValue("MULTIPLE_CHOICE");
//            exampleRow.createCell(4).setCellValue(1.0);
//
//            // Write workbook to response output stream
//            workbook.write(response.getOutputStream());
//            workbook.close(); // Close workbook to release resource
//
//        } else {
//            // Set response headers for CSV download
//            response.setContentType("text/csv");
//            response.setHeader("Content-Disposition", "attachment; filename=question-template.csv");
//
//            // Write CSV content
//            PrintWriter writer = response.getWriter();
//
//            // Write header row
//            writer.println(String.join(",", headers));
//
//            // Write multiple example rows with proper CSV formatting (quoted values)
//            writer.println("\"What is 2+2?\",\"1,2,3,4\",\"4\",\"MULTIPLE_CHOICE\",\"1.0\"");
//            writer.println("\"What is 4+4?\",\"1,2,8,4\",\"8\",\"MULTIPLE_CHOICE\",\"1.0\"");
//            writer.println("\"What is 3+3?\",\"1,6,3,4\",\"6\",\"MULTIPLE_CHOICE\",\"1.0\"");
//
//            writer.flush(); // Ensure all data is written to the response
//        }
//    }




    /**
     * Downloads a comprehensive question template file in either CSV or Excel format.
     * Provides sample data for all question types to guide users.
     *
     * @param testId The ID of the test (for context)
     * @param format Desired file format ("csv" or "excel") - defaults to "csv"
     * @param response HttpServletResponse to write the file to
     * @throws IOException if file writing fails
     */
    @GetMapping("/download-question-template")
    public void downloadQuestionTemplate(
            @PathVariable Long testId,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {

        log.info("Downloading question template for test ID: {} in {} format", testId, format);

        // Define comprehensive column headers for the template
        String[] headers = {
                "Text", "Choices", "Correct Answer", "Type", "Max Marks",
                "Difficulty", "Category", "Explanation", "Media Type", "Media Caption"
        };

        if ("excel".equalsIgnoreCase(format)) {
            // Set response headers for Excel download
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=question-template-" + testId + ".xlsx");

            // Create Excel workbook and sheet
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Questions Template");

                // Create header row with column names
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // Create example rows with sample data for different question types
                createExampleRow(sheet, 1, "What is 2+2?", "1,2,3,4", "4", "MULTIPLE_CHOICE", "1.0", "EASY", "Mathematics", "Basic arithmetic addition", "NONE", "");
                createExampleRow(sheet, 2, "Which are prime numbers?", "2,4,6,7", "2,7", "MULTIPLE_SELECT", "2.0", "MEDIUM", "Mathematics", "Prime numbers are divisible only by 1 and themselves", "NONE", "");
                createExampleRow(sheet, 3, "The capital of France is Paris", "True,False", "True", "TRUE_FALSE", "1.0", "EASY", "Geography", "Paris is the capital city of France", "IMAGE", "Map of France");
                createExampleRow(sheet, 4, "The process of plants making food is called ______", "", "Photosynthesis", "FILL_IN_THE_BLANK", "1.5", "MEDIUM", "Biology", "Plants use sunlight to convert carbon dioxide and water into glucose", "NONE", "");
                createExampleRow(sheet, 5, "Discuss the impact of AI on society", "", "AI transforms industries...", "ESSAY", "5.0", "HARD", "Technology", "Consider ethical, economic, and social aspects", "NONE", "");

                // Auto-size columns for better readability
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write workbook to response output stream
                workbook.write(response.getOutputStream());
            }

        } else {
            // Set response headers for CSV download
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition",
                    "attachment; filename=question-template-" + testId + ".csv");

            // Write CSV content
            PrintWriter writer = response.getWriter();

            // Write header row
            writer.println(String.join(",", headers));

            // Write multiple example rows with proper CSV formatting
            writer.println("\"What is 2+2?\",\"1,2,3,4\",\"4\",\"MULTIPLE_CHOICE\",\"1.0\",\"EASY\",\"Mathematics\",\"Basic arithmetic addition\",\"NONE\",\"\"");
            writer.println("\"Which are prime numbers?\",\"2,4,6,7\",\"2,7\",\"MULTIPLE_SELECT\",\"2.0\",\"MEDIUM\",\"Mathematics\",\"Prime numbers are divisible only by 1 and themselves\",\"NONE\",\"\"");
            writer.println("\"The capital of France is Paris\",\"True,False\",\"True\",\"TRUE_FALSE\",\"1.0\",\"EASY\",\"Geography\",\"Paris is the capital city of France\",\"IMAGE\",\"Map of France\"");
            writer.println("\"The process of plants making food is called ______\",\"\",\"Photosynthesis\",\"FILL_IN_THE_BLANK\",\"1.5\",\"MEDIUM\",\"Biology\",\"Plants use sunlight to convert carbon dioxide and water into glucose\",\"NONE\",\"\"");
            writer.println("\"Discuss the impact of AI on society\",\"\",\"AI transforms industries, creates new jobs, raises ethical questions...\",\"ESSAY\",\"5.0\",\"HARD\",\"Technology\",\"Consider ethical, economic, and social aspects\",\"NONE\",\"\"");

            writer.flush();
        }

        log.info("Question template downloaded successfully for test ID: {}", testId);
    }




    /**
     * Get all questions for a test with pagination and sorting
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllQuestions(
            @PathVariable Long testId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<QuestionDto> questionsPage = questionService.getQuestionsByTest(testId, pageable);

        return createPagedResponse(questionsPage);
    }

    /**
     * Search questions by keyword with pagination within a test
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchQuestions(
            @PathVariable Long testId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "text,asc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<QuestionDto> questionsPage = questionService.searchQuestions(testId, keyword, pageable);

        return createPagedResponse(questionsPage);
    }

    /**
     * Get questions by type with pagination within a test
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getQuestionsByType(
            @PathVariable Long testId,
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "text,asc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<QuestionDto> questionsPage = questionService.getQuestionsByType(testId, type, pageable);

        return createPagedResponse(questionsPage);
    }

    /**
     * Get questions by marks range with pagination within a test
     */
    @GetMapping("/marks-range")
    public ResponseEntity<Map<String, Object>> getQuestionsByMarksRange(
            @PathVariable Long testId,
            @RequestParam(required = false) Double minMarks,
            @RequestParam(required = false) Double maxMarks,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "maxMarks,asc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<QuestionDto> questionsPage = questionService.getQuestionsByMarksRange(testId, minMarks, maxMarks, pageable);

        return createPagedResponse(questionsPage);
    }

    /**
     * Advanced search with multiple criteria within a test
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<Map<String, Object>> advancedSearch(
            @PathVariable Long testId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minMarks,
            @RequestParam(required = false) Double maxMarks,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<QuestionDto> questionsPage = questionService.findByAdvancedSearch(testId, keyword, type, minMarks, maxMarks, pageable);

        return createPagedResponse(questionsPage);
    }

    /**
     * ENHANCED: Updated the existing endpoint to support basic pagination
     * while maintaining backward compatibility
     */
    @GetMapping("")
    public ResponseEntity<?> getByTest(
            @PathVariable Long testId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        // Backward compatibility: if no pagination params, return list as before
        if (page == null && size == null) {
            return ResponseEntity.ok(questionService.getQuestionsByTest(testId));
        }

        // New: if pagination params provided, return paginated result
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<QuestionDto> result = questionService.getQuestionsByTest(testId, pageable);
        return ResponseEntity.ok(createPagedResponse(result).getBody());
    }

    /**
     * Helper method to create standardized paginated response
     */
    private ResponseEntity<Map<String, Object>> createPagedResponse(Page<QuestionDto> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("questions", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("pageSize", page.getSize());
        response.put("hasNext", page.hasNext());
        response.put("hasPrevious", page.hasPrevious());
        response.put("isFirst", page.isFirst());
        response.put("isLast", page.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create Pageable from request parameters
     */
    private Pageable createPageable(int page, int size, String[] sort) {
        if (sort.length >= 2) {
            String sortField = sort[0];
            Sort.Direction sortDirection = Sort.Direction.fromString(sort[1]);
            return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

    }



    /**
     * Helper method to create example rows in Excel template
     */
    private void createExampleRow(Sheet sheet, int rowNum, String text, String choices,
                                  String correctAnswer, String type, String maxMarks,
                                  String difficulty, String category, String explanation,
                                  String mediaType, String mediaCaption) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(text);
        row.createCell(1).setCellValue(choices);
        row.createCell(2).setCellValue(correctAnswer);
        row.createCell(3).setCellValue(type);
        row.createCell(4).setCellValue(maxMarks);
        row.createCell(5).setCellValue(difficulty);
        row.createCell(6).setCellValue(category);
        row.createCell(7).setCellValue(explanation);
        row.createCell(8).setCellValue(mediaType);
        row.createCell(9).setCellValue(mediaCaption);
    }
}