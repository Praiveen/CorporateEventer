package com.example.CorporateEventer.services;

import com.example.CorporateEventer.entities.Meeting;
import com.example.CorporateEventer.repositories.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    public List<Meeting> findAll() {
        return meetingRepository.findAll();
    }

    public Optional<Meeting> findById(Long id) {
        return meetingRepository.findById(id);
    }

    public Meeting save(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    public void deleteById(Long id) {
        meetingRepository.deleteById(id);
    }
} 