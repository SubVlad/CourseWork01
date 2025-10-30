package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class SettlingPersonStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int settlingPersonStatusId;
    private String settlingPersonStatusName;
    @OneToMany(mappedBy = "settlingPersonStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettlingPerson> settlingPersons = new ArrayList<>();
    public void addSettlingPerson(SettlingPerson settlingPerson){
        settlingPersons.add(settlingPerson);
        settlingPerson.setSettlingPersonStatus(this);
    }
    public void removeSettlingPerson(SettlingPerson settlingPerson){
        settlingPersons.remove(settlingPerson);
        settlingPerson.setSettlingPersonStatus(null);
    }
}
