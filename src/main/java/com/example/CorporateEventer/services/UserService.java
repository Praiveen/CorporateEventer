package com.example.CorporateEventer.services;

import com.example.CorporateEventer.entities.*;
import com.example.CorporateEventer.repositories.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // public Optional<User> getUserFromToken(String token) {
    //     String username = JwtService.extractUsername(token);
        
    //     // Допустим, что у вас есть метод для поиска пользователя по имени
    //     return findByEmail(username);
    // }

    public Optional<User> findByEmail(String username) {
        return userRepository.findByEmail(username);
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);

        return users;
    }
}
