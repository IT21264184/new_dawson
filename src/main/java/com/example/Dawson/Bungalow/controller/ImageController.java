package com.example.Dawson.Bungalow.controller;

import com.example.Dawson.Bungalow.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * GET /api/images/{id}
     * Serves the raw image bytes. Frontend uses this URL directly in <img src="...">.
     * Example: <img src="http://localhost:8080/api/images/abc123" />
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        try {
            byte[] imageData = imageService.getImageById(id);
            String contentType = imageService.getContentType(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageData);
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}