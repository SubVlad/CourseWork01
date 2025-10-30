package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userId;
    private String surname;
    private String name;
    private String patronymic;
    private int contactPhoneNumber;
    private String email;
    @ManyToOne
    @JoinColumn(name="cityId")
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private City departureCity;
    private int passportSeriesNumber;
    @Column(unique = true)
    private String login;
    private String password;
    @ManyToOne
    @JoinColumn(name="userRoleId")
    private UserRole userRole;
    @OneToMany(mappedBy = "clientMakingBooking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
    public void addBooking(Booking booking){
        bookings.add(booking);
        booking.setClientMakingBooking(this);
    }
    public void removeBooking(Booking booking){
        bookings.remove(booking);
        booking.setClientMakingBooking(null);
    }
    public String getFIO(){
        return surname +" "+ name +" "+ patronymic;
    }
    public City getCity(){
        return departureCity;
    }
    public void setCity(City city){
        this.departureCity = city;
    }
}
