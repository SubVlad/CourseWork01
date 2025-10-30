package com.SubVlad01.demo.repo;

import com.SubVlad01.demo.models.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Integer> {

}
