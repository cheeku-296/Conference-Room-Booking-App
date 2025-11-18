package com.conference.booking.repository;

import com.conference.booking.entity.Booking;
import com.conference.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);

}
