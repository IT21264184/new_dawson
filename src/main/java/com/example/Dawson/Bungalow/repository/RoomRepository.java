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

    List<Room> findByType(String type);

    List<Room> findByCapacityGreaterThanEqual(int guests);

    List<Room> findByPricePerNightLessThanEqual(double maxPrice);

    List<Room> findByIsActiveTrue();
}