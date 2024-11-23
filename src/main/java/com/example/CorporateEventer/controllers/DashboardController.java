package com.example.CorporateEventer.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CorporateEventer.dto.CompanyRequestDTO;
import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.Notification;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.services.CompanyService;
import com.example.CorporateEventer.services.NotificationService;
import com.example.CorporateEventer.services.UserService;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/dashboard")
@RestController
public class DashboardController {

    @Autowired
    private UserService userService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private NotificationService notificationService;


    @PostMapping("/starter/createCompany/newcompany")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        Authentication authentication = userService.userInfoFromSecurity();
        User currentUser = (User) authentication.getPrincipal();
        company.setDirector(currentUser.getUserId());
        currentUser.setCompany(company);

        Company savedCompany = companyService.save(company);
        userService.save(currentUser);
        return ResponseEntity.ok(savedCompany);
    }


    @GetMapping("/companies")
    public ResponseEntity<List<Map<String, Object>>> getAllCompanies() {
        List<Company> companies = companyService.findAll();
        
        List<Map<String, Object>> companyDTOs = companies.stream()
            .map(company -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", company.getCompanyId());
                dto.put("name", company.getCompanyName());
                dto.put("address", company.getAddress());
                dto.put("directorId", company.getDirector());
                return dto;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(companyDTOs);
    }

    @PostMapping("/companies/request")
    public ResponseEntity<?> createCompanyRequest(@RequestBody CompanyRequestDTO requestDTO) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            Optional<Company> companyOptional = companyService.findById(requestDTO.getCompanyId());
            if (!companyOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Компания не найдена");
            }
            Company company = companyOptional.get();
            if (notificationService.existsBySenderAndCompanyAndIsCompleted(currentUser, company, false)) {
                return ResponseEntity.badRequest().body("У вас уже есть активная заявка на вступление в эту компанию");
            }
            
            if (currentUser.getCompany() != null) {
                return ResponseEntity.badRequest().body("Вы уже состоите в компании");
            }
            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setCompany(company);
            notification.setReceiver(userService.findById(company.getDirector().intValue()).get());
            notification.setMessage("Пользователь " + currentUser.getFirstName() + ' ' + currentUser.getLastName() + " хочет присоединиться к компании");
            notification.setSendDate(LocalDateTime.now());
            notification.setCompleted(false);
            
            notificationService.save(notification);
            return ResponseEntity.ok("Заявка успешно отправлена");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании заявки: " + e.getMessage());
        }
    }
    

    @GetMapping("/received")
    public ResponseEntity<List<Map<String, Object>>> getReceivedNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        List<Notification> notifications = notificationService.findByReceiverAndIsCompleted(currentUser, false);
        
        List<Map<String, Object>> notificationDTOs = notifications.stream()
            .map(notification -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", notification.getNotificationId());
                dto.put("message", notification.getMessage());
                dto.put("senderName", notification.getSender().getUsername());
                dto.put("companyName", notification.getCompany().getCompanyName());
                dto.put("sendDate", notification.getSendDate().toString());
                return dto;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(notificationDTOs);
    }

    

    
}
