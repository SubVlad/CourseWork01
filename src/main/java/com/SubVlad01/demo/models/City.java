package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int cityId;
    private String cityName;

    @OneToMany(mappedBy = "departureCity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();
    public void addUser(User user){
        users.add(user);
        user.setCity(this);
    }
    public void removeUser(User user){
        users.remove(user);
        user.setCity(null);
    }
}
