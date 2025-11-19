// Package declaration - organizes classes within the config package
package com.almubaraksuleiman.cbts.resource;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.resource.FileStorageService;
import com.almubaraksuleiman.cbts.resource.LocalFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for file storage services.
 *
 * This configuration sets up the file storage infrastructure based on
 * application properties and environment configuration.
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@Configuration
public class FileStorageConfig {

    /**
     * Storage type configured from application properties.
     * Determines which storage implementation to use.
     */
    @Value("${file.storage-type:LOCAL}")
    private String storageType;

    /**
     * Configures the appropriate FileStorageService based on configuration.
     *
     * @return FileStorageService The configured file storage service
     */
    @Bean
    public FileStorageService fileStorageService() {
        // For now, we'll use LocalFileStorageService
        // In a production system, you would switch based on storageType
        return new LocalFileStorageService();

        // Future enhancement: Support multiple storage providers
        /*
        switch (storageType.toUpperCase()) {
            case "AWS_S3":
                return new AwsS3StorageService();
            case "AZURE_BLOB":
                return new AzureBlobStorageService();
            case "GCP_STORAGE":
                return new GcpStorageService();
            case "LOCAL":
            default:
                return new LocalFileStorageService();
        }
        */
    }
}