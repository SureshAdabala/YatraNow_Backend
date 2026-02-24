package com.yatranow.service;

import com.yatranow.exception.ImageProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public byte[] processImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageProcessingException("Image file is required");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ImageProcessingException(
                    "Invalid image format. Only JPEG and PNG are allowed");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageProcessingException(
                    "Image file size exceeds maximum limit of 5MB");
        }

        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to process image: " + e.getMessage());
        }
    }

    public byte[] processImageOptional(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return processImage(file);
    }
}
