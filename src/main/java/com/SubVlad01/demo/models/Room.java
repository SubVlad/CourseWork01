package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int roomId;
    @ManyToOne
    @JoinColumn(name="roomTypeId")
    private RoomType roomType;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
    public void addBooking(Booking booking){
        bookings.add(booking);
        booking.setRoom(this);
    }
    public void removeBooking(Booking booking){
        bookings.remove(booking);
        booking.setRoom(null);
    }
    
}
