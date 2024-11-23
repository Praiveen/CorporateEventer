package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.Notification;
import com.example.CorporateEventer.entities.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsBySenderAndCompanyAndIsCompleted(User sender, Company company, boolean isCompleted);
    List<Notification> findByReceiverAndIsCompletedOrderBySendDateDesc(User receiver, boolean isCompleted);

} 