// Package declaration - organizes classes within the resource package
package com.almubaraksuleiman.cbts.resource;

// Import statements - required dependencies
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * File Storage Service Interface for multi-cloud support.
 *
 * This service provides abstraction for file storage operations across
 * different cloud providers and local storage. It enables seamless
 * integration with various storage backends while maintaining a consistent
 * API for file operations.
 *
 * Key Features:
 * - Multi-cloud support (Local, AWS S3, Azure Blob, Google Cloud Storage)
 * - Consistent file operations across different storage providers
 * - File validation and security checks
 * - Storage type detection and configuration
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
public interface FileStorageService {

    /**
     * Stores a file and returns the storage path/URL.
     * Validates file type, size, and security before storage.
     *
     * @param file The multipart file to store
     * @return String The storage path, URL, or identifier for the stored file
     * @throws FileStorageException if file storage fails
     * @throws FileValidationException if file validation fails
     */
    String storeFile(MultipartFile file);

    /**
     * Deletes a file from storage using its path or URL.
     * Handles different storage backends transparently.
     *
     * @param filePath The path, URL, or identifier of the file to delete
     * @return boolean True if deletion was successful, false otherwise
     * @throws FileStorageException if file deletion fails
     */
    boolean deleteFile(String filePath);

    /**
     * Retrieves a file as a Spring Resource for downloading or streaming.
     * Supports both local files and cloud storage objects.
     *
     * @param filePath The path, URL, or identifier of the file to retrieve
     * @return Resource The file as a Spring Resource
     * @throws FileNotFoundException if file does not exist
     * @throws FileStorageException if file retrieval fails
     */
    Resource loadFileAsResource(String filePath);

    /**
     * Gets the storage type currently being used.
     * Useful for client-side URL generation and storage-specific operations.
     *
     * @return StorageType The current storage type
     */
    StorageType getStorageType();

    /**
     * Validates if a file path/URL belongs to the current storage provider.
     * Helps determine if a file can be managed by this service instance.
     *
     * @param filePath The file path or URL to validate
     * @return boolean True if the file can be managed by this storage provider
     */
    boolean isValidFile(String filePath);

    /**
     * Gets the maximum allowed file size for this storage provider.
     * Useful for client-side validation and error messages.
     *
     * @return long Maximum file size in bytes
     */
    long getMaxFileSize();

    /**
     * Gets the supported file types for this storage provider.
     * Returns MIME types or file extensions that are allowed.
     *
     * @return String[] Array of supported file types
     */
    String[] getSupportedFileTypes();
}