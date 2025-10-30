package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int roomTypeId;
    private String roomTypeName;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();
    public void addRoom(Room room){
        rooms.add(room);
        room.setRoomType(this);
    }
    public void removeRoom(Room room){
        rooms.remove(room);
        room.setRoomType(null);
    }

    
}
