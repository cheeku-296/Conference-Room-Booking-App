package com.conference.booking.service;

import com.conference.booking.dto.BookingRequest;
import com.conference.booking.dto.BookingResponse;
import com.conference.booking.entity.Booking;
import com.conference.booking.entity.BookingStatus;
import com.conference.booking.entity.Room;
import com.conference.booking.entity.User;
import com.conference.booking.repository.BookingRepository;
import com.conference.booking.repository.RoomRepository;
import com.conference.booking.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;

    @Mock
    RoomRepository roomRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    BookingService bookingService;

    private AutoCloseable mocks;

    @Test
    void createBooking_success_createsAndReturnsBooking() {
        String username = "alice";
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("a@ex.com");

        Room room = new Room();
        room.setId(10L);
        room.setName("Room A");
        room.setCapacity(5);

        LocalDateTime start = LocalDateTime.of(2025, 11, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 20, 12, 0);

        BookingRequest request = new BookingRequest();
        request.setRoomId(room.getId());
        request.setStartTime(start);
        request.setEndTime(end);
        request.setPurpose("Meeting");
        request.setAttendeesCount(3);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        // emulate repository saving - assign ID back
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(100L);
            return b;
        });

        Booking created = bookingService.createBooking(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getUser()).isEqualTo(user);
        assertThat(created.getRoom()).isEqualTo(room);
        assertThat(created.getStartTime()).isEqualTo(start);
        assertThat(created.getEndTime()).isEqualTo(end);
        assertThat(created.getPurpose()).isEqualTo("Meeting");
        assertThat(created.getAttendeesCount()).isEqualTo(3);
        assertThat(created.getStatus()).isEqualTo(BookingStatus.PENDING);

        verify(userRepository).findByUsername(username);
        verify(roomRepository).findById(room.getId());
        verify(bookingRepository).findAll();
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_userNotFound_throws() {
        String username = "bob";
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        BookingRequest req = new BookingRequest();
        req.setRoomId(1L);
        req.setStartTime(LocalDateTime.now());
        req.setEndTime(LocalDateTime.now().plusHours(1));
        req.setAttendeesCount(1);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(roomRepository, bookingRepository);
    }

    @Test
    void createBooking_roomNotFound_throws() {
        String username = "charlie";
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        User user = new User();
        user.setId(2L);
        user.setUsername(username);

        BookingRequest req = new BookingRequest();
        req.setRoomId(999L);
        req.setStartTime(LocalDateTime.now());
        req.setEndTime(LocalDateTime.now().plusHours(1));
        req.setAttendeesCount(1);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roomRepository.findById(req.getRoomId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room not found");

        verify(userRepository).findByUsername(username);
        verify(roomRepository).findById(req.getRoomId());
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void createBooking_attendeesExceedCapacity_throws() {
        String username = "dave";
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        User user = new User();
        user.setId(3L);
        user.setUsername(username);

        Room room = new Room();
        room.setId(5L);
        room.setCapacity(2);

        BookingRequest req = new BookingRequest();
        req.setRoomId(room.getId());
        req.setStartTime(LocalDateTime.now());
        req.setEndTime(LocalDateTime.now().plusHours(2));
        req.setAttendeesCount(4); // exceeding capacity

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.createBooking(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Attendees count exceeds room capacity");

        verify(userRepository).findByUsername(username);
        verify(roomRepository).findById(room.getId());
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void createBooking_conflictingApprovedBooking_throws() {
        String username = "eve";
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        User user = new User();
        user.setId(4L);
        user.setUsername(username);

        Room room = new Room();
        room.setId(20L);
        room.setCapacity(10);

        LocalDateTime start = LocalDateTime.of(2025, 11, 25, 9, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 25, 10, 0);

        BookingRequest req = new BookingRequest();
        req.setRoomId(room.getId());
        req.setStartTime(start);
        req.setEndTime(end);
        req.setAttendeesCount(5);

        Booking existing = new Booking();
        existing.setId(200L);
        existing.setRoom(room);
        existing.setStartTime(start.minusMinutes(30)); // overlaps
        existing.setEndTime(end.plusMinutes(30));
        existing.setStatus(BookingStatus.APPROVED);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(bookingRepository.findAll()).thenReturn(Collections.singletonList(existing));

        assertThatThrownBy(() -> bookingService.createBooking(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room is already booked for this time slot");

        verify(userRepository).findByUsername(username);
        verify(roomRepository).findById(room.getId());
        verify(bookingRepository).findAll();
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBookingStatus() {
    }

    @Test
    void getUserBookings() {
    }

    @Test
    void getAllBookings() {
    }

    @Test
    void getBookingStatistics() {
    }
}