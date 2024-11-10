package com.example.CorporateEventer.services;

import com.example.CorporateEventer.entities.MeetingParticipant;
import com.example.CorporateEventer.repositories.MeetingParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MeetingParticipantService {

    @Autowired
    private MeetingParticipantRepository meetingParticipantRepository;

    public List<MeetingParticipant> findAll() {
        return meetingParticipantRepository.findAll();
    }

    public Optional<MeetingParticipant> findById(Long id) {
        return meetingParticipantRepository.findById(id);
    }

    public MeetingParticipant save(MeetingParticipant meetingParticipant) {
        return meetingParticipantRepository.save(meetingParticipant);
    }

    public void deleteById(Long id) {
        meetingParticipantRepository.deleteById(id);
    }
} 