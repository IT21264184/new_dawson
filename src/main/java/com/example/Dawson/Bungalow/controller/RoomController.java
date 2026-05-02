package com.example.Dawson.Bungalow.controller;

import com.example.Dawson.Bungalow.dto.RoomRequest;
import com.example.Dawson.Bungalow.dto.RoomResponse;
import com.example.Dawson.Bungalow.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // ── PUBLIC ENDPOINTS ──────────────────────────────────────────────────────

    // GET /api/rooms
    // Optional params: ?type=Deluxe&maxPrice=12000&guests=2&checkIn=2025-08-01&checkOut=2025-08-05
    // When checkIn + checkOut are provided, each room in the response includes
    // an `available` boolean computed from real booking data.
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

    // GET /api/rooms/{id}?checkIn=2025-08-01&checkOut=2025-08-05
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

    // ── ADMIN ENDPOINTS ───────────────────────────────────────────────────────

    // GET /api/rooms/admin/all — all rooms, no date filters needed for admin dashboard
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomResponse>> getAllRoomsAdmin() {
        return ResponseEntity.ok(roomService.getAllRooms(null, null));
    }

    // POST /api/rooms/admin — create new room
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest request) {
        try {
            RoomResponse room = roomService.createRoom(request);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/rooms/admin/{id} — update room details
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRoom(@PathVariable String id,
                                        @RequestBody RoomRequest request) {
        try {
            RoomResponse room = roomService.updateRoom(id, request);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/rooms/admin/{id}
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
}