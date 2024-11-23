package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.Meeting;
import com.example.CorporateEventer.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    
} 