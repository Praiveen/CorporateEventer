package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    // Optional<User> findById(Long Id);
}
