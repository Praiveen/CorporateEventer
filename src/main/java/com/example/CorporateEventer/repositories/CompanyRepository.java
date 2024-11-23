package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company findByDirector(Long id);
}
