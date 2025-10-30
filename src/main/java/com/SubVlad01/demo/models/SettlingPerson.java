package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class SettlingPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int settlingPersonId;
    private String surname;
    private String name;
    private String patronymic;
    @ManyToOne
    @JoinColumn(name="settlingPersonStatusId")
    private SettlingPersonStatus settlingPersonStatus;

    @OneToMany(mappedBy = "settlingPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettlingPersonByBooking> settlingPersonsByBooking = new ArrayList<>();
    public void addSettlingPersonByBooking(SettlingPersonByBooking settlingPersonByBooking){
        settlingPersonsByBooking.add(settlingPersonByBooking);
        settlingPersonByBooking.setSettlingPerson(this);
    }
    public void removeSettlingPersonByBooking(SettlingPersonByBooking settlingPersonByBooking){
        settlingPersonsByBooking.remove(settlingPersonByBooking);
        settlingPersonByBooking.setSettlingPerson(null);
    }
}
