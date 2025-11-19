// Package declaration - organizes classes within the resource package
package com.almubaraksuleiman.cbts.resource;

// Import statements - required dependencies
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local file system implementation of FileStorageService.
 * 
 * This service handles file storage operations on the local file system.
 * It provides secure file storage with proper directory structure,
 * file naming, and access control.
 *
 * Features:
 * - Secure file storage with UUID-based filenames
 * - Directory creation and management
 * - File type validation and size limits
 * - Resource loading for file downloads
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    /**
     * Base directory for file storage configured from application properties.
     * Defaults to "uploads" directory in current working directory.
     */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Maximum file size allowed for upload in bytes.
     * Defaults to 50MB.
     */
    @Value("${file.max-size:52428800}")
    private long maxFileSize;

    /**
     * Supported file types for upload.
     * Defaults to common image, video, and audio formats.
     */
    private final String[] supportedFileTypes = {
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "video/mp4", "video/webm", "video/ogg",
        "audio/mpeg", "audio/wav", "audio/ogg"
    };

    /**
     * Stores a file on the local file system with secure naming.
     * Validates file type and size before storage.
     *
     * @param file The multipart file to store
     * @return String The relative path to the stored file
     * @throws FileStorageException if storage directory creation fails
     * @throws FileValidationException if file validation fails
     */
    @Override
    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate secure filename
            String filename = generateSecureFilename(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(filename);

            // Copy file to target location
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }

    /**
     * Deletes a file from the local file system.
     *
     * @param filePath The relative path of the file to delete
     * @return boolean True if deletion was successful, false if file didn't exist
     * @throws FileStorageException if file deletion fails
     */
    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path fileToDelete = Paths.get(uploadDir).resolve(filePath).normalize();
            
            if (!Files.exists(fileToDelete)) {
                return false;
            }

            // Security check: ensure the file is within the upload directory
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!fileToDelete.toAbsolutePath().normalize().startsWith(uploadPath)) {
                throw new FileStorageException("Cannot delete file outside upload directory");
            }

            return Files.deleteIfExists(fileToDelete);

        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + filePath, ex);
        }
    }

    /**
     * Loads a file as a Spring Resource for downloading.
     *
     * @param filePath The relative path of the file to load
     * @return Resource The file as a Spring Resource
     * @throws FileStorageException if file loading fails
     */
    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = Paths.get(uploadDir).resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found or not readable: " + filePath);
            }

        } catch (MalformedURLException ex) {
            throw new FileStorageException("Could not load file: " + filePath, ex);
        }
    }

    /**
     * Returns the storage type as LOCAL.
     *
     * @return StorageType.LOCAL
     */
    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }

    /**
     * Validates if a file path belongs to the local storage.
     * For local storage, checks if file exists and is within upload directory.
     *
     * @param filePath The file path to validate
     * @return boolean True if file exists and is within upload directory
     */
    @Override
    public boolean isValidFile(String filePath) {
        try {
            Path file = Paths.get(uploadDir).resolve(filePath).normalize();
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            
            return Files.exists(file) && 
                   file.toAbsolutePath().normalize().startsWith(uploadPath);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns the maximum allowed file size.
     *
     * @return long Maximum file size in bytes
     */
    @Override
    public long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Returns the supported file types.
     *
     * @return String[] Array of supported MIME types
     */
    @Override
    public String[] getSupportedFileTypes() {
        return supportedFileTypes.clone();
    }

    // Helper Methods

    /**
     * Validates file type, size, and security.
     *
     * @param file The multipart file to validate
     * @throws FileValidationException if validation fails
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new FileValidationException("File size exceeds maximum allowed size: " + 
                (maxFileSize / (1024 * 1024)) + "MB");
        }

        // Check file type
        String contentType = file.getContentType();
        boolean supported = false;
        for (String supportedType : supportedFileTypes) {
            if (supportedType.equals(contentType)) {
                supported = true;
                break;
            }
        }

        if (!supported) {
            throw new FileValidationException("File type not supported: " + contentType);
        }

        // Check filename security
        String filename = file.getOriginalFilename();
        if (filename != null && filename.contains("..")) {
            throw new FileValidationException("Filename contains invalid path sequence: " + filename);
        }
    }

    /**
     * Generates a secure filename using UUID and original file extension.
     *
     * @param originalFilename The original filename
     * @return String Secure filename with UUID and extension
     */
    private String generateSecureFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Gets the absolute path of the upload directory.
     * Useful for administrative operations and file management.
     *
     * @return String Absolute path of upload directory
     */
    public String getUploadDirectory() {
        return Paths.get(uploadDir).toAbsolutePath().toString();
    }
}