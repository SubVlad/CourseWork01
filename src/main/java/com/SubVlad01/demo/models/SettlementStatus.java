package com.SubVlad01.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class SettlementStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int settlementStatusId;
    private String settlementStatusName;

    @OneToMany(mappedBy = "settlementStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Settlement> settlements = new ArrayList<>();
    public void addSettlement(Settlement settlement){
        settlements.add(settlement);
        settlement.setSettlementStatus(this);
    }
    public void removeSettlement(Settlement settlement){
        settlements.remove(settlement);
        settlement.setSettlementStatus(null);
    }
}
