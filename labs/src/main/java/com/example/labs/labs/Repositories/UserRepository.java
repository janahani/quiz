package com.example.labs.labs.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.labs.labs.Models.User;

public interface UserRepository extends JpaRepository<User,Integer>{
    User findByUsername(String username);
}
