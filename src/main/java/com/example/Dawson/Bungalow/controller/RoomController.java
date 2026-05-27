package com.example.Dawson.Bungalow.controller;

import com.example.Dawson.Bungalow.dto.RoomRequest;
import com.example.Dawson.Bungalow.dto.RoomResponse;
import com.example.Dawson.Bungalow.service.ImageService;
import com.example.Dawson.Bungalow.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ImageService imageService;


    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRooms(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        List<RoomResponse> rooms = roomService.filterRooms(type, maxPrice, guests, checkIn, checkOut);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        try {
            RoomResponse room = roomService.getRoomById(id, checkIn, checkOut);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomResponse>> getAllRoomsAdmin() {
        return ResponseEntity.ok(roomService.getAllRoomsAdmin());
    }


    @PatchMapping("/admin/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleRoomStatus(@PathVariable String id) {
        try {
            RoomResponse room = roomService.toggleRoomStatus(id);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRoom(
            @RequestParam("roomNumber") String roomNumber,
            @RequestParam("type") String type,
            @RequestParam("pricePerNight") double pricePerNight,
            @RequestParam("capacity") int capacity,
            @RequestParam("description") String description,
            @RequestParam(value = "amenities", required = false) String amenitiesJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            RoomRequest request = buildRoomRequest(roomNumber, type, pricePerNight,
                    capacity, description, amenitiesJson);


            List<String> imageIds = (images != null && !images.isEmpty())
                    ? imageService.uploadImages(images)
                    : List.of();

            RoomResponse room = roomService.createRoom(request, imageIds);
            return ResponseEntity.ok(room);
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping(value = "/admin/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRoom(
            @PathVariable String id,
            @RequestParam("roomNumber") String roomNumber,
            @RequestParam("type") String type,
            @RequestParam("pricePerNight") double pricePerNight,
            @RequestParam("capacity") int capacity,
            @RequestParam("description") String description,
            @RequestParam(value = "amenities", required = false) String amenitiesJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            RoomRequest request = buildRoomRequest(roomNumber, type, pricePerNight,
                    capacity, description, amenitiesJson);


            List<String> newImageIds = (images != null && !images.isEmpty())
                    ? imageService.uploadImages(images)
                    : null; // null = keep existing images

            RoomResponse room = roomService.updateRoom(id, request, newImageIds);
            return ResponseEntity.ok(room);
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRoom(@PathVariable String id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok(Map.of("message", "Room deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @PostMapping(value = "/admin/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadRoomImages(
            @PathVariable String id,
            @RequestParam("images") List<MultipartFile> files) {
        try {
            List<String> imageIds = imageService.uploadImages(files);
            RoomResponse room = roomService.addImagesToRoom(id, imageIds);
            return ResponseEntity.ok(room);
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{roomId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRoomImage(
            @PathVariable String roomId,
            @PathVariable String imageId) {
        try {
            RoomResponse room = roomService.removeImageFromRoom(roomId, imageId);
            imageService.deleteImage(imageId);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    private RoomRequest buildRoomRequest(String roomNumber, String type, double pricePerNight,
                                         int capacity, String description, String amenitiesJson) {
        RoomRequest req = new RoomRequest();
        req.setRoomNumber(roomNumber);
        req.setType(type);
        req.setPricePerNight(pricePerNight);
        req.setCapacity(capacity);
        req.setDescription(description);


        if (amenitiesJson != null && !amenitiesJson.isBlank()) {
            try {

                String cleaned = amenitiesJson.replaceAll("[\\[\\]\"]", "").trim();
                if (!cleaned.isBlank()) {
                    req.setAmenities(Arrays.asList(cleaned.split(",\\s*")));
                }
            } catch (Exception e) {
                req.setAmenities(List.of());
            }
        }
        return req;
    }
}