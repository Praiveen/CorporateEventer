package com.example.CorporateEventer.services;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    public Company save(Company company) {
        return companyRepository.save(company);
    }

    public void deleteById(Long id) {
        companyRepository.deleteById(id);
    }

    public Company findByDirector(Long userId) {
        return companyRepository.findByDirector(userId);
    }
}
