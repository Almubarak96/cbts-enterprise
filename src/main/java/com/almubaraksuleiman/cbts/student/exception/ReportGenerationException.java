package com.almubaraksuleiman.cbts.student.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ReportGenerationException - Exception for PDF generation failures
 * 
 * Thrown when PDF report generation fails due to technical issues
 * Automatically returns HTTP 500 status when thrown from controller
 * 
 * @ResponseStatus Sets HTTP status code to 500 (INTERNAL_SERVER_ERROR)
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ReportGenerationException extends RuntimeException {
    public ReportGenerationException(String message) {
        super(message);
    }
    
    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}