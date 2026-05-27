package com.example.Dawson.Bungalow.repository;

import com.example.Dawson.Bungalow.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Booking> findByRoomIdOrderByCheckInDateAsc(String roomId);

    List<Booking> findByStatusOrderByCreatedAtDesc(String status);

    List<Booking> findAllByOrderByCreatedAtDesc();


    @Query("{ 'roomId': ?0, 'status': { $in: ['pending', 'confirmed'] }, 'checkInDate': { $lt: ?2 }, 'checkOutDate': { $gt: ?1 } }")
    List<Booking> findOverlappingBookings(String roomId, LocalDate checkIn, LocalDate checkOut);


    @Query("{ 'roomId': ?0, '_id': { $ne: ?1 }, 'status': { $in: ['pending', 'confirmed'] }, 'checkInDate': { $lt: ?3 }, 'checkOutDate': { $gt: ?2 } }")
    List<Booking> findOverlappingBookingsExcluding(String roomId, String excludeBookingId, LocalDate checkIn, LocalDate checkOut);
}
