package com.example.CorporateEventer.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    private String companyName;
    private String address;

    @OneToOne
    @JoinColumn(name = "director_id")
    private User director;

    @OneToMany(mappedBy = "company")
    private List<Department> departments;
}

