package com.example.CorporateEventer.services;

import com.example.CorporateEventer.dto.LoginUserDto;
import com.example.CorporateEventer.dto.RegisterUserDto;
import com.example.CorporateEventer.entities.*;
import com.example.CorporateEventer.repositories.*;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDto input) {
        var user = new User()
            .setFullName(input.getFullName())
            .setFirstName(input.getFirstName())
            .setLastName(input.getLastName())
            .setEmail(input.getEmail())
            .setPassword(passwordEncoder.encode(input.getPassword()));

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                input.getEmail(),
                input.getPassword()
            )
        );

        return userRepository.findByEmail(input.getEmail()).orElseThrow();
    }

    public boolean saveUser(RegisterUserDto registerUserDto) {
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            return false;
        }
        return true;
    }
}
