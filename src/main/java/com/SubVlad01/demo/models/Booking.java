package com.SubVlad01.demo.models;

import com.SubVlad01.demo.repo.SettlingPersonByBookingRepository;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import javax.persistence.Transient;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int bookingId;
    @ManyToOne
    @JoinColumn(name="userId")
    private User clientMakingBooking;
    @CreatedDate
    private Date bookingMakingDate;
    private Date plannedSettlementDate;
    private Date plannedDepartureDate;
    @ManyToOne
    @JoinColumn(name="roomId")
    private Room room;
    @ManyToOne
    @JoinColumn(name="bookingStatusId")
    private BookingStatus bookingStatus;
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettlingPersonByBooking> settlingPersonsByBooking = new ArrayList<>();
    public void addSettlingPersonByBooking(SettlingPersonByBooking settlingPersonByBooking){
        settlingPersonsByBooking.add(settlingPersonByBooking);
        settlingPersonByBooking.setBooking(this);
    }
    public void removeSettlingPersonByBooking(SettlingPersonByBooking settlingPersonByBooking){
        settlingPersonsByBooking.remove(settlingPersonByBooking);
        settlingPersonByBooking.setBooking(null);
    }
}
