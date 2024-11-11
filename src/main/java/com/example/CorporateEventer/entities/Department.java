package com.example.CorporateEventer.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    private String departmentName;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToMany(mappedBy = "department")
    private List<SubDepartment> subDepartments;

    // @OneToMany(mappedBy = "department")
    // private List<User> users;
}


