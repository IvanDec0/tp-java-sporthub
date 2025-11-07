package com.java.sportshub.daos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.java.sportshub.models.User;

@Repository
public interface UserDAO extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrUserName(String email, String userName);

    Optional<User> findByPhoneNumber(String phoneNumber);

}
