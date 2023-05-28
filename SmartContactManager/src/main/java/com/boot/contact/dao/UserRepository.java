package com.boot.contact.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boot.contact.entities.User;

public interface UserRepository extends JpaRepository<User, Integer>{

}
