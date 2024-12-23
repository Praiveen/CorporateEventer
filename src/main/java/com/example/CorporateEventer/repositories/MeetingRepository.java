package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.Meeting;
import com.example.CorporateEventer.entities.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
        List<Meeting> findByParticipantsContaining(User participant);

        @Query("SELECT m FROM Meeting m WHERE :user MEMBER OF m.participants OR m.organizer = :user")
        List<Meeting> findByParticipantOrOrganizer(@Param("user") User user);

        List<Meeting> findByStatus(String status);

} 