package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SettlingPersonByBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int settlingPersonByBookingId;
    @ManyToOne
    @JoinColumn(name="settlingPersonId")
    private SettlingPerson settlingPerson;
    @ManyToOne
    @JoinColumn(name="bookingId")
    private Booking booking;

    
}
