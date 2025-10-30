package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class BookingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int bookingStatusId;
    private String bookingStatusName;

    @OneToMany(mappedBy = "bookingStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
    public void addBooking(Booking booking){
        bookings.add(booking);
        booking.setBookingStatus(this);
    }
    public void removeBooking(Booking booking){
        bookings.remove(booking);
        booking.setBookingStatus(null);
    }
}
