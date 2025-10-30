package com.SubVlad01.demo.repo;

import com.SubVlad01.demo.models.Settlement;
import com.SubVlad01.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Integer> {
    Iterable<Settlement> findByBookingClientMakingBooking(User user);

}