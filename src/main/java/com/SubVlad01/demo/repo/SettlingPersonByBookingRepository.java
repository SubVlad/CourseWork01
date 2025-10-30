package com.SubVlad01.demo.repo;

import com.SubVlad01.demo.models.Booking;
import com.SubVlad01.demo.models.SettlingPerson;
import com.SubVlad01.demo.models.SettlingPersonByBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SettlingPersonByBookingRepository extends JpaRepository<SettlingPersonByBooking, Integer> {
    List<SettlingPersonByBooking> findByBooking(Booking booking);

    //Optional<SettlingPerson> findBySettlingPersonByBooking(SettlingPersonByBooking settlingPersonByBooking);
}