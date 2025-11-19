package com.almubaraksuleiman.cbts.api;


import com.almubaraksuleiman.cbts.examiner.model.AnalyticsData;
import com.almubaraksuleiman.cbts.examiner.model.AnalyticsFilters;
import com.almubaraksuleiman.cbts.examiner.model.QuestionAnalysis;
import com.almubaraksuleiman.cbts.examiner.model.StudentPerformance;
import com.almubaraksuleiman.cbts.examiner.service.impl.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor

public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/tests/{testId}")
    public ResponseEntity<AnalyticsData> getTestAnalytics(
            @PathVariable Long testId,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        AnalyticsFilters filters = AnalyticsFilters.builder()
                .dateRange(dateRange)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        
        AnalyticsData analytics = analyticsService.getTestAnalytics(testId, filters);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/tests/{testId}/export")
    public ResponseEntity<String> exportAnalyticsReport(
            @PathVariable Long testId,
            @RequestParam(required = false) String dateRange) {
        
        // For now, return success message - implement PDF export later
        return ResponseEntity.ok("Export functionality will be implemented soon for test ID: " + testId);
    }





    // In AnalyticsController.java - Add this endpoint

    @GetMapping("/tests/{testId}/questions")
    public ResponseEntity<Page<QuestionAnalysis>> getQuestionAnalysis(
            @PathVariable Long testId,
            @RequestParam(required = false) String dateRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "accuracy") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        AnalyticsFilters filters = AnalyticsFilters.builder()
                .dateRange(dateRange)
                .build();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        Page<QuestionAnalysis> questionAnalysis = analyticsService
                .getQuestionAnalysisWithPagination(testId, filters, pageable);

        return ResponseEntity.ok(questionAnalysis);
    }



    @GetMapping("/tests/{testId}/students")
    public ResponseEntity<Page<StudentPerformance>> getStudentPerformance(
            @PathVariable Long testId,
            @RequestParam(required = false) String dateRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "score") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        AnalyticsFilters filters = AnalyticsFilters.builder()
                .dateRange(dateRange)
                .build();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        Page<StudentPerformance> studentPerformance = analyticsService
                .getStudentPerformanceWithPagination(testId, filters, pageable, search, status);

        return ResponseEntity.ok(studentPerformance);
    }





    @GetMapping("/tests/{testId}/export/pdf")
    public ResponseEntity<ByteArrayResource> exportPdfReport(
            @PathVariable Long testId,
            @RequestParam(required = false) String dateRange) {

        AnalyticsFilters filters = AnalyticsFilters.builder()
                .dateRange(dateRange)
                .build();

        AnalyticsData analytics = analyticsService.getTestAnalytics(testId, filters);
        byte[] pdfBytes = analyticsService.generatePdfReport(analytics);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=analytics-report-test-" + testId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    @GetMapping("/tests/{testId}/export/excel")
    public ResponseEntity<ByteArrayResource> exportExcelReport(
            @PathVariable Long testId,
            @RequestParam(required = false) String dateRange) {

        AnalyticsFilters filters = AnalyticsFilters.builder()
                .dateRange(dateRange)
                .build();

        AnalyticsData analytics = analyticsService.getTestAnalytics(testId, filters);
        byte[] excelBytes = analyticsService.generateExcelReport(analytics);

        ByteArrayResource resource = new ByteArrayResource(excelBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=analytics-report-test-" + testId + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelBytes.length)
                .body(resource);
    }


    @GetMapping("/tests/{testId}/export/csv")
    public ResponseEntity<ByteArrayResource> exportCsvReport(
            @PathVariable Long testId,
            @RequestParam(required = false) String dateRange) {

        AnalyticsFilters filters = AnalyticsFilters.builder()
                .dateRange(dateRange)
                .build();

        AnalyticsData analytics = analyticsService.getTestAnalytics(testId, filters);
        byte[] csvBytes = analyticsService.generateCsvReport(analytics);

        ByteArrayResource resource = new ByteArrayResource(csvBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=analytics-report-test-" + testId + ".csv")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(csvBytes.length)
                .body(resource);
    }

}

