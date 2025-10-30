package com.SubVlad01.demo;

import com.SubVlad01.demo.models.User;
import com.SubVlad01.demo.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByLogin(login);
        return user.map(MyUserDetails::new)
                .orElseThrow(()->new UsernameNotFoundException("user " + login + " not found :_("));

    }
}
