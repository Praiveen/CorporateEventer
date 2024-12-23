package com.example.CorporateEventer.services;

import com.example.CorporateEventer.entities.Event;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    public void deleteById(Long id) {
        eventRepository.deleteById(id);
    }

    public List<Event> findByParticipant(User participant) {
        return eventRepository.findByParticipantsContaining(participant);
    }

    public List<Event> findByParticipantOrCreator(User user) {
        return eventRepository.findByParticipantOrCreatedBy(user);
    }
    

    @Scheduled(fixedRate = 3600000)
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();
        
        List<Event> events = eventRepository.findByStatus(Event.STATUS_PLANNED);
        
        for (Event event : events) {
            if (event.getEndTime().isBefore(now)) {
                event.setStatus(Event.STATUS_COMPLETED);
                eventRepository.save(event);
            }
        }
    }
} 