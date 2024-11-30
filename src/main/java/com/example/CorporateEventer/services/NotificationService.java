package com.example.CorporateEventer.services;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.Notification;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> findById(Long notificationId){
        return notificationRepository.findById(notificationId);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public boolean existsBySenderAndCompanyAndIsCompleted(User sender, Company company, boolean isCompleted) {
        return notificationRepository.existsBySenderAndCompanyAndIsCompleted(sender, company, isCompleted);
    }

    public List<Notification> findByReceiverAndIsCompleted(User receiver, boolean isCompleted) {
        return notificationRepository.findByReceiverAndIsCompletedOrderBySendDateDesc(receiver, isCompleted);
    }

} 