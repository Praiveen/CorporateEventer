package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends JpaRepository<EventParticipant, Long> {
} 