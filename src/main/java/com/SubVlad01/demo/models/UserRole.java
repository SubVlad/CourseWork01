package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userRoleId;
    private String userRoleName;
    @OneToMany(mappedBy = "userRole", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();
    public void addUser(User user){
        users.add(user);
        user.setUserRole(this);
    }
    public void removeUser(User user){
        users.remove(user);
        user.setUserRole(null);
    }
}
