package com.example.Dawson.Bungalow.service;

import com.example.Dawson.Bungalow.dto.RoomRequest;
import com.example.Dawson.Bungalow.dto.RoomResponse;
import com.example.Dawson.Bungalow.model.Room;
import com.example.Dawson.Bungalow.repository.BookingRepository;
import com.example.Dawson.Bungalow.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ImageService imageService;

    // Set this in application.properties: app.base-url=http://localhost:8080
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ── Get all rooms ─────────────────────────────────────────────────────────
    public List<RoomResponse> getAllRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream()
                .map(room -> {
                    Boolean available = computeAvailability(room.getId(), checkIn, checkOut);
                    return RoomResponse.from(room, available, toImageUrls(room.getImages()));
                })
                .toList();
    }

    // ── Filter rooms ──────────────────────────────────────────────────────────
    public List<RoomResponse> filterRooms(String type, Double maxPrice,
                                          Integer guests, LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream()
                .filter(r -> type == null || type.isBlank() || r.getType().equalsIgnoreCase(type))
                .filter(r -> maxPrice == null || r.getPricePerNight() <= maxPrice)
                .filter(r -> guests == null || r.getCapacity() >= guests)
                .map(room -> {
                    Boolean available = computeAvailability(room.getId(), checkIn, checkOut);
                    return RoomResponse.from(room, available, toImageUrls(room.getImages()));
                })
                .toList();
    }

    // ── Get single room ───────────────────────────────────────────────────────
    public RoomResponse getRoomById(String id, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Boolean available = computeAvailability(id, checkIn, checkOut);
        return RoomResponse.from(room, available, toImageUrls(room.getImages()));
    }

    // ── Availability check ────────────────────────────────────────────────────
    public boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return true;
        return bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut).isEmpty();
    }

    // ── Create room ───────────────────────────────────────────────────────────
    public RoomResponse createRoom(RoomRequest request, List<String> imageIds) {
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new RuntimeException("Room number " + request.getRoomNumber() + " already exists");
        }
        Room room = new Room();
        mapRequestToRoom(request, room);
        room.setImages(imageIds); // set GridFS IDs directly
        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }

    // ── Update room ───────────────────────────────────────────────────────────
    public RoomResponse updateRoom(String id, RoomRequest request, List<String> newImageIds) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getRoomNumber().equals(request.getRoomNumber())
                && roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new RuntimeException("Room number " + request.getRoomNumber() + " already exists");
        }

        mapRequestToRoom(request, room);

        // Only replace images if new ones were uploaded
        if (newImageIds != null) {
            imageService.deleteImages(room.getImages()); // delete old ones from GridFS
            room.setImages(newImageIds);
        }

        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }

    // ── Delete room (also deletes its images from GridFS) ─────────────────────
    public void deleteRoom(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Clean up all GridFS images before deleting the room
        imageService.deleteImages(room.getImages());
        roomRepository.deleteById(id);
    }

    // ── Add images to a room ──────────────────────────────────────────────────
    public RoomResponse addImagesToRoom(String roomId, List<String> imageIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<String> existing = room.getImages() != null
                ? new ArrayList<>(room.getImages())
                : new ArrayList<>();
        existing.addAll(imageIds);
        room.setImages(existing);

        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }

    // ── Remove one image from a room ──────────────────────────────────────────
    public RoomResponse removeImageFromRoom(String roomId, String imageId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<String> images = new ArrayList<>(
                room.getImages() != null ? room.getImages() : List.of()
        );
        images.remove(imageId);
        room.setImages(images);

        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Boolean computeAvailability(String roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return null;
        return bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut).isEmpty();
    }

    /**
     * Converts GridFS image IDs → full serving URLs.
     * e.g. "abc123" → "http://localhost:8080/api/images/abc123"
     * The frontend can use these directly in <img src="..."> tags.
     */
    private List<String> toImageUrls(List<String> imageIds) {
        if (imageIds == null) return List.of();
        return imageIds.stream()
                .map(id -> baseUrl + "/api/images/" + id)
                .toList();
    }

    private void mapRequestToRoom(RoomRequest req, Room room) {
        room.setRoomNumber(req.getRoomNumber());
        room.setType(req.getType());
        room.setPricePerNight(req.getPricePerNight());
        room.setCapacity(req.getCapacity());
        room.setDescription(req.getDescription());
        room.setAmenities(req.getAmenities());
        // images intentionally excluded — managed via GridFS separately
    }
}