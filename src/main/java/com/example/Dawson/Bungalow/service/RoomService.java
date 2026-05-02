package com.example.Dawson.Bungalow.service;

import com.example.Dawson.Bungalow.dto.RoomRequest;
import com.example.Dawson.Bungalow.dto.RoomResponse;
import com.example.Dawson.Bungalow.model.Room;
import com.example.Dawson.Bungalow.repository.BookingRepository;
import com.example.Dawson.Bungalow.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // ── Get all rooms ─────────────────────────────────────────────────────────
    // If checkIn + checkOut are provided, each room's `available` field is computed.
    // If no dates are provided, `available` is returned as null.
    public List<RoomResponse> getAllRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream()
                .map(room -> {
                    Boolean available = computeAvailability(room.getId(), checkIn, checkOut);
                    return RoomResponse.from(room, available);
                })
                .toList();
    }

    // ── Filter rooms with optional type / maxPrice / guests / dates ───────────
    public List<RoomResponse> filterRooms(String type, Double maxPrice,
                                          Integer guests, LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .filter(r -> type == null || type.isBlank() || r.getType().equalsIgnoreCase(type))
                .filter(r -> maxPrice == null || r.getPricePerNight() <= maxPrice)
                .filter(r -> guests == null || r.getCapacity() >= guests)
                .map(room -> {
                    Boolean available = computeAvailability(room.getId(), checkIn, checkOut);
                    return RoomResponse.from(room, available);
                })
                .toList();
    }

    // ── Get single room by ID ─────────────────────────────────────────────────
    public RoomResponse getRoomById(String id, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Boolean available = computeAvailability(id, checkIn, checkOut);
        return RoomResponse.from(room, available);
    }

    // ── Check if a specific room is available for given dates ─────────────────
    public boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return true;
        return bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut).isEmpty();
    }

    // ── Create room (admin only) ──────────────────────────────────────────────
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new RuntimeException("Room number " + request.getRoomNumber() + " already exists");
        }
        Room room = new Room();
        mapRequestToRoom(request, room);
        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved, null);
    }

    // ── Update room (admin only) ──────────────────────────────────────────────
    public RoomResponse updateRoom(String id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // If room number is changing, make sure the new one is not already taken
        if (!room.getRoomNumber().equals(request.getRoomNumber())
                && roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new RuntimeException("Room number " + request.getRoomNumber() + " already exists");
        }

        mapRequestToRoom(request, room);
        return RoomResponse.from(roomRepository.save(room), null);
    }

    // ── Delete room (admin only) ──────────────────────────────────────────────
    public void deleteRoom(String id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found");
        }
        roomRepository.deleteById(id);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    // Returns null if no dates given (availability unknown),
    // true if no overlapping bookings exist, false otherwise.
    private Boolean computeAvailability(String roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return null;
        return bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut).isEmpty();
    }

    private void mapRequestToRoom(RoomRequest req, Room room) {
        room.setRoomNumber(req.getRoomNumber());
        room.setType(req.getType());
        room.setPricePerNight(req.getPricePerNight());
        room.setCapacity(req.getCapacity());
        room.setDescription(req.getDescription());
        room.setImages(req.getImages());
        room.setAmenities(req.getAmenities());
    }
}