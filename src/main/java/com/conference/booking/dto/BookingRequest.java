package com.conference.booking.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingRequest {
    private Long roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private int attendeesCount;
}