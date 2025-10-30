package com.SubVlad01.demo.repo;

import com.SubVlad01.demo.models.SettlingPerson;
import com.SubVlad01.demo.models.SettlingPersonByBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SettlingPersonRepository extends JpaRepository<SettlingPerson, Integer> {
}