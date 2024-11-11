package com.example.CorporateEventer.controllers;

// import com.tericcabrel.authapi.entities.User;
// import com.tericcabrel.authapi.dtos.LoginUserDto;
// import com.tericcabrel.authapi.dtos.RegisterUserDto;
// import com.tericcabrel.authapi.responses.LoginResponse;
// import com.tericcabrel.authapi.services.AuthenticationService;
// import com.tericcabrel.authapi.services.JwtService;

import com.example.CorporateEventer.entities.*;
import com.example.CorporateEventer.services.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;

    private final AuthenticationService authenticationService;
    
    // private final UserService userService;


    
    

    

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        // this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        // LoginResponse loginResponse = new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirationTime());
        LoginResponse loginResponse = new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    // @GetMapping("/me")
    // public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    //     User user = userService.findByEmail(userDetails.getUsername());
    //     return ResponseEntity.ok(user);
    // }
}

