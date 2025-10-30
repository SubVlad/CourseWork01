package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Entity
@Getter
@Setter
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int settlementId;
    @ManyToOne
    @JoinColumn(name="bookingId")
    private Booking booking;
    private Date actualDepartureDate;
    private Date actualSettlementDate;
    @ManyToOne
    @JoinColumn(name="settlementStatusId")
    private SettlementStatus settlementStatus;

    
}
