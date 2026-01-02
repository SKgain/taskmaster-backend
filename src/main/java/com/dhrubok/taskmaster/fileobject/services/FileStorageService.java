package com.dhrubok.taskmaster.fileobject.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads/profiles}")
    private String uploadDir;

    /**
     * Store uploaded file
     * @param file - The multipart file to store
     * @param userId - User ID to create subdirectory
     * @return The URL path to access the stored file
     */
    public String storeFile(MultipartFile file, String userId) throws IOException {
        // Create directory structure: uploads/profiles/{userId}/
        Path uploadPath = Paths.get(uploadDir, userId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // Copy file to the target location
        Path targetLocation = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Return the file URL path
        return String.format("/uploads/profiles/%s/%s", userId, filename);
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads")) {
                // Extract path from URL
                String relativePath = fileUrl.replace("/uploads/profiles/", "");
                Path filePath = Paths.get(uploadDir).resolve(relativePath);

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("File deleted successfully: {}", filePath.toAbsolutePath());
                } else {
                    log.warn("File not found for deletion: {}", filePath.toAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileUrl, e);
        }
    }

    public void validateImageFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Check allowed extensions
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.matches("jpg|jpeg|png|gif|webp|bmp")) {
                throw new IllegalArgumentException("Only JPG, JPEG, PNG, GIF, WEBP, and BMP files are allowed");
            }
        }
    }
}