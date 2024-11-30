package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.Event;
import com.example.CorporateEventer.entities.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByParticipantsContaining(User participant);

}
