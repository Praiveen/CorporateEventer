package com.example.CorporateEventer.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingId;

    private String topic;
    private String agenda;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    // @ManyToOne
    // @JoinColumn(name = "organizer_id")
    // private User organizer;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // @ManyToMany
    // @JoinTable(
    //     name = "meeting_participants",
    //     joinColumns = @JoinColumn(name = "meeting_id"),
    //     inverseJoinColumns = @JoinColumn(name = "user_id")
    // )
    // private List<User> participants;
}

