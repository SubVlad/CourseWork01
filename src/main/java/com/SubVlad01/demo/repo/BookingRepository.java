package com.SubVlad01.demo.repo;

import com.SubVlad01.demo.models.Booking;
import org.springframework.data.repository.CrudRepository;
public interface BookingRepository extends CrudRepository <Booking, Integer> {

        }
