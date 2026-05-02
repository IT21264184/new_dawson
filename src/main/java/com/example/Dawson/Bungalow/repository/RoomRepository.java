package com.example.Dawson.Bungalow.repository;

import com.example.Dawson.Bungalow.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    // Filter by room type
    List<Room> findByType(String type);

    // Filter by capacity (rooms that fit at least N guests)
    List<Room> findByCapacityGreaterThanEqual(int guests);

    // Filter by price up to a max
    List<Room> findByPricePerNightLessThanEqual(double maxPrice);
}