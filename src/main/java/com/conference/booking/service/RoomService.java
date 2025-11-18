package com.conference.booking.service;

import com.conference.booking.entity.Room;
import com.conference.booking.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Room not found with id: " + id));
    }

    public Room createRoom(Room room) {

        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room updatedRoom) {
        Room room = getRoomById(id);
        room.setName(updatedRoom.getName());
        room.setCapacity(updatedRoom.getCapacity());
        room.setLocation(updatedRoom.getLocation());
        room.setAmenities(updatedRoom.getAmenities());
        room.setAvailable(updatedRoom.isAvailable());
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}
