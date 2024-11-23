package com.example.CorporateEventer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.services.CompanyService;
import com.example.CorporateEventer.services.UserService;

@RequestMapping("/notification")
@RestController
public class NotificationController {
    
}
