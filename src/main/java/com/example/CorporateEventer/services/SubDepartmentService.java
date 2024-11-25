package com.example.CorporateEventer.services;

import com.example.CorporateEventer.entities.SubDepartment;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.repositories.SubDepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubDepartmentService {

    @Autowired
    private SubDepartmentRepository subDepartmentRepository;

    public List<SubDepartment> findAll() {
        return subDepartmentRepository.findAll();
    }

    public Optional<SubDepartment> findById(Long id) {
        return subDepartmentRepository.findById(id);
    }

    public SubDepartment save(SubDepartment subDepartment) {
        return subDepartmentRepository.save(subDepartment);
    }

    public void deleteById(Long id) {
        subDepartmentRepository.deleteById(id);
    }

    public boolean isUserSubDepartmentManager(User user) {
        return subDepartmentRepository.existsByManager(user);
    }
}
