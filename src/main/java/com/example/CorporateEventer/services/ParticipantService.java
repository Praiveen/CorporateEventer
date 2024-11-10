package com.example.CorporateEventer.services;

import com.example.CorporateEventer.entities.EventParticipant;
import com.example.CorporateEventer.repositories.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    public List<EventParticipant> findAll() {
        return participantRepository.findAll();
    }

    public Optional<EventParticipant> findById(Long id) {
        return participantRepository.findById(id);
    }

    public EventParticipant save(EventParticipant participant) {
        return participantRepository.save(participant);
    }

    public void deleteById(Long id) {
        participantRepository.deleteById(id);
    }
} 