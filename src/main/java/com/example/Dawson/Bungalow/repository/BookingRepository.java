package com.example.Dawson.Bungalow.repository;

import com.example.Dawson.Bungalow.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    // All bookings by a specific user
    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);

    // All bookings for a specific room
    List<Booking> findByRoomIdOrderByCheckInDateAsc(String roomId);

    // All bookings by status (admin use)
    List<Booking> findByStatusOrderByCreatedAtDesc(String status);

    // All bookings ordered by newest first (admin dashboard)
    List<Booking> findAllByOrderByCreatedAtDesc();

    // Overlap check — used by availability logic.
    // Two date ranges overlap when: existingCheckIn < newCheckOut AND existingCheckOut > newCheckIn
    // Only pending and confirmed bookings block a room; cancelled ones do not.
    @Query("{ 'roomId': ?0, 'status': { $in: ['pending', 'confirmed'] }, 'checkInDate': { $lt: ?2 }, 'checkOutDate': { $gt: ?1 } }")
    List<Booking> findOverlappingBookings(String roomId, LocalDate checkIn, LocalDate checkOut);

    // Same overlap check but excluding a specific booking ID (used when editing a booking)
    @Query("{ 'roomId': ?0, '_id': { $ne: ?1 }, 'status': { $in: ['pending', 'confirmed'] }, 'checkInDate': { $lt: ?3 }, 'checkOutDate': { $gt: ?2 } }")
    List<Booking> findOverlappingBookingsExcluding(String roomId, String excludeBookingId, LocalDate checkIn, LocalDate checkOut);
}
