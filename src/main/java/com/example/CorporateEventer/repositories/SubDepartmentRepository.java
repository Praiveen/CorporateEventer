package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.SubDepartment;
import com.example.CorporateEventer.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubDepartmentRepository extends JpaRepository<SubDepartment, Long> {
    boolean existsByManager(User manager);
    SubDepartment findByManager(User currentUser);
}
