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
    public static final String STATUS_PLANNED = "PLANNED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingId;

    private String topic;
    private String agenda;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToMany
    private List<User> participants;



    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(this.status);
    }
    
    public boolean isPlanned() {
        return STATUS_PLANNED.equals(this.status);
    }
    
    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(this.status);
    }
}

