// Package declaration - organizes classes within the API package
package com.almubaraksuleiman.cbts.api;

// Import statements - required dependencies for the controller
import com.almubaraksuleiman.cbts.dto.TestDto;
import com.almubaraksuleiman.cbts.examiner.service.TestService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing tests in the CBTS system.
 * Provides endpoints for CRUD operations, bulk upload, and template downloads.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
// Allow requests from Angular dev server
@RestController // Marks this class as a REST controller
@RequestMapping("/api/admin/tests") // Base path for all test-related endpoints
public class TestController {

    // Service layer dependency for test business logic
    private final TestService testService;

    /**
     * Constructor-based dependency injection for TestService.
     *
     * @param testService The test service implementation
     */
    public TestController(TestService testService) {
        this.testService = testService;
    }

    /**
     * Creates a new test from the provided DTO.
     *
     * @param testDto Data Transfer Object containing test information
     * @return TestDto The created test with generated ID
     */
    @PostMapping
    public TestDto createTest(@RequestBody TestDto testDto) {
        return testService.createTest(testDto);
    }

    /**
     * Updates an existing test with new data.
     *
     * @param id The ID of the test to update
     * @param testDto DTO containing updated test data
     * @return ResponseEntity<TestDto> The updated test with OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<TestDto> updateTest(@PathVariable Long id, @RequestBody TestDto testDto) {
        return ResponseEntity.ok(testService.editTest(id, testDto));
    }

    /**
     * Retrieves all tests from the system.
     *
     * @return List<TestDto> List of all tests
     */
    @GetMapping
    public List<TestDto> getAllTests() {
        return testService.getAllTests();
    }

    /**
     * Retrieves a specific test by its ID.
     *
     * @param id The ID of the test to retrieve
     * @return TestDto The test data
     */
    @GetMapping("/{id}")
    public TestDto getTest(@PathVariable Long id) {
        return testService.getTestById(id);
    }

    /**
     * Deletes a test by its ID.
     *
     * @param id The ID of the test to delete
     */
    @DeleteMapping("/{id}")
    public void deleteTest(@PathVariable Long id) {
        testService.deleteTest(id);
    }

    /**
     * Bulk uploads tests from a file (CSV or Excel).
     * Supports importing multiple tests at once from structured files.
     *
     * @param file The uploaded file containing test data
     * @return ResponseEntity<String> Success or error message
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadTests(@RequestParam("file") MultipartFile file) {
        try {
            // Process the bulk upload through service layer
            testService.bulkUploadTests(file);

            // Debug log (consider using proper logging framework)
            System.out.println("OIUPLOADEDD");

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "message", "Tests uploaded successfully!",
                    "count", testService.getAllTests().size() // Or get count from service
            ));
        } catch (Exception e) {
            // Return error response with server error status
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Downloads a blank template file for test creation in either CSV or Excel format.
     * Provides users with the correct format for bulk test uploads.
     *
     * @param format Desired file format ("csv" or "excel") - defaults to "excel"
     * @param response HttpServletResponse (unused but kept for consistency)
     * @return ResponseEntity<byte[]> File download with appropriate headers
     * @throws IOException if file generation fails
     */
    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadBlankTemplate(
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) throws IOException {
        try {
            if ("csv".equalsIgnoreCase(format)) {
                // CSV template with column headers
                String header = "Title,Description,DurationMinutes,ShuffleChoices,NumberOfQuestions,Published,TotalMarks,RandomizeQuestions\n";
                byte[] data = header.getBytes(StandardCharsets.UTF_8);

                // Return CSV file with appropriate headers
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test-template.csv")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .body(data);

            } else {
                // Excel template creation
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Tests");

                // Define column headers for Excel template
                Row headerRow = sheet.createRow(0);
                String[] columns = {"Title", "NumberOfQuestions", "RandomizeQuestions", "ShuffleChoices",
                        "Description", "DurationMinutes", "TotalMarks", "Published"};

                // Create header cells with column names
                for (int i = 0; i < columns.length; i++) {
                    headerRow.createCell(i).setCellValue(columns[i]);
                }

                // Convert workbook to byte array for download
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                workbook.close(); // Clean up resource

                // Return Excel file with appropriate headers
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test-template.xlsx")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(out.toByteArray());
            }

        } catch (Exception e) {
            // Handle any errors during template generation
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating template: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }




    /**
     * Get all tests with pagination and sorting
     */
    @GetMapping("all")
    public ResponseEntity<Map<String, Object>> getAllTests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<TestDto> testsPage = testService.getAllTests(pageable);

        return createPagedResponse(testsPage);
    }




    /**
     * Search tests by keyword with pagination
     */
   // @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTests(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<TestDto> testsPage = testService.searchTests(keyword, pageable);

        return createPagedResponse(testsPage);
    }

    /**
     * Get tests by published status with pagination
     */
    @GetMapping("/status/{published}")
    public ResponseEntity<Map<String, Object>> getTestsByStatus(
            @PathVariable Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<TestDto> testsPage = testService.getTestsByStatus(published, pageable);

        return createPagedResponse(testsPage);
    }

    /**
     * Get tests by duration range with pagination
     */
    @GetMapping("/duration-range")
    public ResponseEntity<Map<String, Object>> getTestsByDurationRange(
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "durationMinutes,asc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<TestDto> testsPage = testService.getTestsByDurationRange(minDuration, maxDuration, pageable);

        return createPagedResponse(testsPage);
    }




    /**
     * Advanced search with multiple criteria
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<TestDto> testsPage = testService.findByAdvancedSearch(keyword, published, minDuration, maxDuration, pageable);

        return createPagedResponse(testsPage);
    }


    /**
     * Helper method to create standardized paginated response
     */
    private ResponseEntity<Map<String, Object>> createPagedResponse(Page<TestDto> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("tests", page.getContent());
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
}