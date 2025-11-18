package com.conference.booking.controller;

import com.conference.booking.dto.BookingRequest;
import com.conference.booking.dto.BookingResponse;
import com.conference.booking.entity.Booking;
import com.conference.booking.entity.BookingStatus;
import com.conference.booking.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Booking createBooking(@RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public List<BookingResponse> getUserBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingService.getUserBookings(username);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Booking updateBookingStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        return bookingService.updateBookingStatus(id, status);
    }
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> getBookingStats() {
        return bookingService.getBookingStatistics();
    }
}