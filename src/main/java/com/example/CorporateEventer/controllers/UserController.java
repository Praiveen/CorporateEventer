package com.example.CorporateEventer.controllers;


import com.example.CorporateEventer.entities.*;
import com.example.CorporateEventer.services.*;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SubDepartmentService subDepartmentService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/userData")
    public ResponseEntity<Map<String, Object>> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> response = new HashMap<>();
        response.put("email", currentUser.getEmail());
        response.put("firstName", currentUser.getFirstName());
        response.put("lastName", currentUser.getLastName());
        response.put("phone", currentUser.getPhoneNumber());

        String userRole = currentUser.getRoles().stream()
            .findFirst()
            .map(Role::getName)
            .orElse("Роль не назначена");

        System.out.println(userRole);


        if (currentUser.getCompany() != null) {
            response.put("company", currentUser.getCompany().getCompanyName());
        } else {
            response.put("company", null);
        }
        if (currentUser.getDepartment() != null) {
            response.put("department", currentUser.getDepartment().getDepartmentName());
        }  else if ("DEPARTMENT_MANAGER".equals(userRole)) {
            response.put("department", departmentService.findByManagerId(currentUser.getUserId()).get().getDepartmentName());
        } else {
            response.put("department", null);
        }
        if (currentUser.getSubDepartment() != null) {
            response.put("subDepartment", currentUser.getSubDepartment().getSubdepartmentName());
        } else if ("SUBDEPARTMENT_MANAGER".equals(userRole)) {
            SubDepartment managedSubDepartment = subDepartmentService.findByManager(currentUser);
            response.put("subDepartment", Optional.ofNullable(managedSubDepartment)
                    .map(SubDepartment::getSubdepartmentName)
                    .orElse(null));
        } else {
            response.put("subDepartment", null);
        }
        response.put("role", currentUser.getRoles());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/userData/update")
    public ResponseEntity<?> updateUserData(@RequestBody Map<String, String> updateData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            
            Optional.ofNullable(updateData.get("firstName"))
                    .ifPresent(currentUser::setFirstName);
            Optional.ofNullable(updateData.get("lastName"))
                    .ifPresent(currentUser::setLastName);
            Optional.ofNullable(updateData.get("phoneNumber"))
                    .ifPresent(currentUser::setPhoneNumber);
            
            userService.save(currentUser);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при обновлении данных: " + e.getMessage());
        }
    }

}
