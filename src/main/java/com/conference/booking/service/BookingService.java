package com.conference.booking.service;

import com.conference.booking.dto.BookingRequest;
import com.conference.booking.dto.BookingResponse;
import com.conference.booking.entity.*;
import com.conference.booking.repository.BookingRepository;
import com.conference.booking.repository.RoomRepository;
import com.conference.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public Booking createBooking(BookingRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getAttendeesCount() > room.getCapacity()) {
            throw new RuntimeException("Attendees count exceeds room capacity. Maximum: " + room.getCapacity());
        }

        boolean isAvailable = bookingRepository.findAll().stream()
                .filter(b -> b.getRoom().getId().equals(room.getId()))
                .noneMatch(b -> b.getStartTime().isBefore(request.getEndTime()) &&
                        b.getEndTime().isAfter(request.getStartTime()) &&
                        b.getStatus() == BookingStatus.APPROVED);

        if (!isAvailable) {
            throw new RuntimeException("Room is already booked for this time slot");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose());
        booking.setAttendeesCount(request.getAttendeesCount());
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(Long bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public List<BookingResponse> getUserBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Booking> bookings = bookingRepository.findByUser(user);

        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setRoomId(booking.getRoom().getId());
        response.setRoomName(booking.getRoom().getName());
        response.setUserName(booking.getUser().getUsername());
        response.setUserEmail(booking.getUser().getEmail());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setPurpose(booking.getPurpose());
        response.setAttendeesCount(booking.getAttendeesCount());
        response.setStatus(booking.getStatus());
        return response;
    }
    public Map<String, Long> getBookingStatistics() {
        Map<String, Long> stats = new HashMap<>();
        List<Booking> allBookings = bookingRepository.findAll();

        stats.put("total", (long) allBookings.size());
        stats.put("pending", allBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count());
        stats.put("approved", allBookings.stream().filter(b -> b.getStatus() == BookingStatus.APPROVED).count());
        stats.put("rejected", allBookings.stream().filter(b -> b.getStatus() == BookingStatus.REJECTED).count());

        return stats;
    }
}