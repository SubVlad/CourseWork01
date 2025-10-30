package com.SubVlad01.demo.repo;

import com.SubVlad01.demo.models.UserRole;
import org.springframework.data.repository.CrudRepository;

public interface UserRoleRepository extends CrudRepository<UserRole, Integer> {
}