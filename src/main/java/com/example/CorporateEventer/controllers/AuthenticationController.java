package com.example.CorporateEventer.controllers;


import com.example.CorporateEventer.dto.LoginUserDto;
import com.example.CorporateEventer.dto.RegisterUserDto;
import com.example.CorporateEventer.entities.*;
import com.example.CorporateEventer.services.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    // @PostMapping("/signup")
    // public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
    //     User registeredUser = authenticationService.signup(registerUserDto);

    //     return ResponseEntity.ok(registeredUser);
    // }

    /*
     * Обработка регистрации
     */
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        if (!registerUserDto.getPassword().equals(registerUserDto.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body("Пароли не совпадают");
        }
        if (registerUserDto.getPassword().length() < 5) {
            return ResponseEntity.badRequest().body("Пароль должен содержать минимум 5 символов");
        }
        if (!authenticationService.saveUser(registerUserDto)) {
            System.out.println("Уже есть");
            return ResponseEntity.badRequest().body("Пользователь с такой почтой уже зарегистрирован");
        }
        
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok("Аккаунт зарегестрирован, теперь можно в него войти!");
    }

    @PostMapping("path")
    public String postMethodName(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    

    // @PostMapping(value = "/regSave", consumes = "application/x-www-form-urlencoded")
    // public String addNewUser(@ModelAttribute("userReg") @Valid User user, BindingResult result, Model model) {
    //     if (result.hasErrors()) {
    //         return "register";
    //     }
    //     if (!user.getPassword().equals(user.getPasswordConfirm())) {
    //         model.addAttribute("Message", "Пароли не совпадают");
    //         System.out.println("pass");
    //         return "register";
    //     }
    //     if (user.getPassword().length() < 5) {
    //         model.addAttribute("Message", "Пароль должен содержать минимум 5 символов");
    //         return "register";
    //     }
    //     if (!userService.saveUser(user, "new")) {
    //         model.addAttribute("Message", "Пользователь с такой почтой уже зарегестрирован");
    //         return "register";
    //     }
    //     model.addAttribute("RegFull", "Аккаунт зарегестрирован, теперь можно в него войти!");
    //     return "register";
    // }

    // @PostMapping("/login")
    // public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
    //     User authenticatedUser = authenticationService.authenticate(loginUserDto);

    //     String jwtToken = jwtService.generateToken(authenticatedUser);

    //     LoginResponse loginResponse = new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirationTime());

    //     return ResponseEntity.ok(loginResponse);
    // }

    /*
     * обработка авторизации
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);

        Cookie cookie = new Cookie("jwtToken", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);

        LoginResponse loginResponse = new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    /*
     * Обработка выхода с аккаунта
     */
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).build();
    }
}