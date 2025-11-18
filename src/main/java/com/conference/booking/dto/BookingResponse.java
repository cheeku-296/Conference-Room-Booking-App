package com.conference.booking.dto;

import com.conference.booking.entity.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private String userName;
    private String userEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private int attendeesCount;
    private BookingStatus status;

}