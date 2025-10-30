package com.SubVlad01.demo.repo;

import com.SubVlad01.demo.models.BookingStatus;
import org.springframework.data.repository.CrudRepository;

public interface BookingStatusRepository extends CrudRepository<BookingStatus, Integer> {

}