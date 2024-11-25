package com.example.CorporateEventer.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@NoArgsConstructor
@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    private String companyName;
    private String address;

    @OneToMany(mappedBy = "company")
    private List<Notification> notifications;

    @JoinColumn(name = "director_id")
    private Long director;

    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
    private List<Department> departments;

    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
    private List<User> users;
}

