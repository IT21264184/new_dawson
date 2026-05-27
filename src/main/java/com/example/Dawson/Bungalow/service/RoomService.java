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

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;


    public List<RoomResponse> getAllRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findByIsActiveTrue();
        return rooms.stream()
                .map(room -> {
                    Boolean available = computeAvailability(room.getId(), checkIn, checkOut);
                    return RoomResponse.from(room, available, toImageUrls(room.getImages()));
                })
                .toList();
    }


    public List<RoomResponse> filterRooms(String type, Double maxPrice,
                                          Integer guests, LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findByIsActiveTrue();
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


    public RoomResponse getRoomById(String id, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));


        if (!room.isActive()) {
            throw new RuntimeException("Room not found");
        }

        Boolean available = computeAvailability(id, checkIn, checkOut);
        return RoomResponse.from(room, available, toImageUrls(room.getImages()));
    }


    public List<RoomResponse> getAllRoomsAdmin() {
        return roomRepository.findAll().stream()
                .map(room -> {
                    Boolean available = computeAvailability(room.getId(), null, null);
                    return RoomResponse.from(room, available, toImageUrls(room.getImages()));
                })
                .toList();
    }


    public RoomResponse toggleRoomStatus(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setActive(!room.isActive());
        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }


    public RoomResponse createRoom(RoomRequest request, List<String> imageIds) {
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new RuntimeException("Room number " + request.getRoomNumber() + " already exists");
        }
        Room room = new Room();
        mapRequestToRoom(request, room);
        room.setImages(imageIds);
        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }


    public RoomResponse updateRoom(String id, RoomRequest request, List<String> newImageIds) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getRoomNumber().equals(request.getRoomNumber())
                && roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new RuntimeException("Room number " + request.getRoomNumber() + " already exists");
        }

        mapRequestToRoom(request, room);

        if (newImageIds != null) {
            imageService.deleteImages(room.getImages());
            room.setImages(newImageIds);
        }

        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }


    public void deleteRoom(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        imageService.deleteImages(room.getImages());
        roomRepository.deleteById(id);
    }


    public RoomResponse addImagesToRoom(String roomId, List<String> imageIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<String> existing = room.getImages() != null
                ? new ArrayList<>(room.getImages()) : new ArrayList<>();
        existing.addAll(imageIds);
        room.setImages(existing);

        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }


    public RoomResponse removeImageFromRoom(String roomId, String imageId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<String> images = new ArrayList<>(
                room.getImages() != null ? room.getImages() : List.of());
        images.remove(imageId);
        room.setImages(images);

        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null, toImageUrls(saved.getImages()));
    }



    private Boolean computeAvailability(String roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return null;
        return bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut).isEmpty();
    }

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
        room.setActive(req.isActive());
    }
}