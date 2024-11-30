package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.Meeting;
import com.example.CorporateEventer.entities.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
        List<Meeting> findByParticipantsContaining(User participant);

} 