package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
} 