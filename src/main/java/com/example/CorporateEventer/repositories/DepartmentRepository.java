package com.example.CorporateEventer.repositories;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.Department;
import com.example.CorporateEventer.entities.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByManager(User manager);
    List<Department> findByCompany(Company company);
    Department findByManager(User manager);
    boolean existsByDepartmentNameAndCompany(String name, Company company);
    @Query("SELECT d FROM Department d WHERE d.manager.userId = :managerId")
    Optional<Department> findByManagerUserId(@Param("managerId") Long managerId);


}

