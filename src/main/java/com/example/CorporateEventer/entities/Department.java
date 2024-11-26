package com.example.CorporateEventer.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@NoArgsConstructor
@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    private String departmentName;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToMany(mappedBy = "department")
    private List<SubDepartment> subDepartments;

    @OneToMany(mappedBy = "department")
    private List<User> users;
}


