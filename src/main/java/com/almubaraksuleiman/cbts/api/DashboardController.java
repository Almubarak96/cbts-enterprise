//package com.almubaraksuleiman.cbts.api;
//
//import com.almubaraksuleiman.cbts.examiner.dto.DashboardStats;
//import com.almubaraksuleiman.cbts.examiner.dto.PlatformOverview;
//import com.almubaraksuleiman.cbts.examiner.service.impl.DashboardService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/dashboard")
//@RequiredArgsConstructor
//@Slf4j
//public class DashboardController {
//
//    private final DashboardService dashboardService;
//
//    @GetMapping("/stats")
//    public ResponseEntity<DashboardStats> getDashboardStats(
//            @RequestParam(required = false) String dateRange) {
//        try {
//            log.info("Fetching dashboard statistics for date range: {}", dateRange);
//            DashboardStats stats = dashboardService.getDashboardStats(dateRange);
//            return ResponseEntity.ok(stats);
//        } catch (Exception e) {
//            log.error("Error fetching dashboard stats: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GetMapping("/overview")
//    public ResponseEntity<PlatformOverview> getPlatformOverview(
//            @RequestParam(required = false) String dateRange) {
//        try {
//            log.info("Fetching platform overview for date range: {}", dateRange);
//            PlatformOverview overview = dashboardService.getPlatformOverview(dateRange);
//            return ResponseEntity.ok(overview);
//        } catch (Exception e) {
//            log.error("Error fetching platform overview: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GetMapping("/examiner")
//    public ResponseEntity<Map<String, Object>> getExaminerDashboard() {
//        try {
//            log.info("Fetching examiner-specific dashboard data");
//            Map<String, Object> examinerData = dashboardService.getExaminerDashboardData();
//            return ResponseEntity.ok(examinerData);
//        } catch (Exception e) {
//            log.error("Error fetching examiner dashboard: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GetMapping("/activity")
//    public ResponseEntity<?> getRecentActivity(
//            @RequestParam(defaultValue = "10") int limit) {
//        try {
//            log.info("Fetching recent activity with limit: {}", limit);
//            return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
//        } catch (Exception e) {
//            log.error("Error fetching recent activity: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GetMapping("/export")
//    public ResponseEntity<byte[]> exportDashboardReport(
//            @RequestParam String format,
//            @RequestParam(required = false) String dateRange) {
//        try {
//            log.info("Exporting dashboard report in {} format", format);
//
//            byte[] reportData = dashboardService.exportDashboardReport(format, dateRange);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(getMediaType(format));
//            headers.setContentDispositionFormData("attachment", getFileName(format, dateRange));
//
//            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
//
//        } catch (Exception e) {
//            log.error("Error exporting dashboard report: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    private MediaType getMediaType(String format) {
//        return switch (format.toLowerCase()) {
//            case "pdf" -> MediaType.APPLICATION_PDF;
//            case "excel" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            case "csv" -> MediaType.parseMediaType("text/csv");
//            default -> MediaType.APPLICATION_OCTET_STREAM;
//        };
//    }
//
//    private String getFileName(String format, String dateRange) {
//        String dateSuffix = dateRange != null ? "_" + dateRange : "";
//        return String.format("dashboard_report%s.%s", dateSuffix, format.toLowerCase());
//    }
//}














package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.examiner.dto.DashboardStats;
import com.almubaraksuleiman.cbts.examiner.dto.PlatformOverview;
import com.almubaraksuleiman.cbts.examiner.service.impl.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats(
            @RequestParam(required = false) String dateRange) {
        try {
            log.info("Fetching dashboard statistics for date range: {}", dateRange);
            DashboardStats stats = dashboardService.getDashboardStats(dateRange);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/overview")
    public ResponseEntity<PlatformOverview> getPlatformOverview(
            @RequestParam(required = false) String dateRange) {
        try {
            log.info("Fetching platform overview for date range: {}", dateRange);
            PlatformOverview overview = dashboardService.getPlatformOverview(dateRange);
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("Error fetching platform overview: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/examiner")
    public ResponseEntity<Map<String, Object>> getExaminerDashboard() {
        try {
            log.info("Fetching examiner-specific dashboard data");
            Map<String, Object> examinerData = dashboardService.getExaminerDashboardData();
            return ResponseEntity.ok(examinerData);
        } catch (Exception e) {
            log.error("Error fetching examiner dashboard: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/activity")
    public ResponseEntity<?> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("Fetching recent activity with limit: {}", limit);
            return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
        } catch (Exception e) {
            log.error("Error fetching recent activity: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDashboardReport(
            @RequestParam String format,
            @RequestParam(required = false) String dateRange) {
        try {
            log.info("Exporting dashboard report in {} format", format);

            byte[] reportData = dashboardService.exportDashboardReport(format, dateRange);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            headers.setContentDispositionFormData("attachment", getFileName(format, dateRange));

            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting dashboard report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType getMediaType(String format) {
        return switch (format.toLowerCase()) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "excel" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "csv" -> MediaType.parseMediaType("text/csv");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private String getFileName(String format, String dateRange) {
        String dateSuffix = dateRange != null ? "_" + dateRange : "";
        return String.format("dashboard_report%s.%s", dateSuffix, format.toLowerCase());
    }
}