package com.example.CorporateEventer.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class SubDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subdepartmentId;

    private String subdepartmentName;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    // @OneToOne
    // @JoinColumn(name = "manager_id")
    // private User manager;

    // @OneToMany(mappedBy = "subDepartment")
    // private List<User> users;
}

