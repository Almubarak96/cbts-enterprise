// Package declaration - organizes classes within the resource package
package com.almubaraksuleiman.cbts.resource;

/**
 * Custom exception for file storage operations.
 * 
 * This exception is thrown when file storage operations fail due to
 * various reasons such as IO errors, permission issues, or storage
 * provider limitations.
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
public class FileStorageException extends RuntimeException {
    
    /**
     * Constructs a new FileStorageException with the specified detail message.
     *
     * @param message The detail message explaining the exception
     */
    public FileStorageException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new FileStorageException with the specified detail message and cause.
     *
     * @param message The detail message explaining the exception
     * @param cause The underlying cause of the exception
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new FileStorageException with the specified cause.
     *
     * @param cause The underlying cause of the exception
     */
    public FileStorageException(Throwable cause) {
        super(cause);
    }
}