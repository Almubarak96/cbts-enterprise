// Package declaration - organizes classes within the resource package
package com.almubaraksuleiman.cbts.resource;

/**
 * Custom exception for file validation failures.
 * 
 * This exception is thrown when uploaded files fail validation checks
 * such as file type, size, security scanning, or format requirements.
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
public class FileValidationException extends RuntimeException {
    
    /**
     * Constructs a new FileValidationException with the specified detail message.
     *
     * @param message The detail message explaining the validation failure
     */
    public FileValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new FileValidationException with the specified detail message and cause.
     *
     * @param message The detail message explaining the validation failure
     * @param cause The underlying cause of the validation failure
     */
    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}