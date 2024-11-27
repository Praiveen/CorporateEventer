package com.example.CorporateEventer.services;

import com.example.CorporateEventer.dto.DepartmentUpdateDTO;
import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.Department;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.repositories.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserService userService;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }

    public boolean isUserDepartmentManager(User user) {
        return departmentRepository.existsByManager(user);
    }

    public List<Department> findByCompany(Company company) {
        return departmentRepository.findByCompany(company);
    }

    public void updateDepartment(Long departmentId, DepartmentUpdateDTO updateDTO) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new RuntimeException("Отдел не найден"));

        if (!department.getDepartmentName().equals(updateDTO.getDepartmentName())) {
            if (departmentRepository.existsByDepartmentNameAndCompany(
                    updateDTO.getDepartmentName(), 
                    department.getCompany())) {
                throw new RuntimeException("Отдел с таким названием уже существует");
            }
            department.setDepartmentName(updateDTO.getDepartmentName());
        }

        if (!department.getManager().getUserId().equals(updateDTO.getHeadId())) {
            User newManager = userService.findById(updateDTO.getHeadId().intValue())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            if (newManager.getUserId().equals(department.getCompany().getDirector())) {
                throw new RuntimeException("Директор компании не может быть руководителем отдела");
            }

            Department otherDepartment = departmentRepository.findByManager(newManager);
            if (otherDepartment != null && !otherDepartment.getDepartmentId().equals(departmentId)) {
                throw new RuntimeException("Выбранный сотрудник уже является руководителем другого отдела");
            }

            // User oldManager = department.getManager();
            // oldManager.setManagedDepartment(null);
            
            department.setManager(newManager);
            // newManager.setManagedDepartment(department);
        }

        departmentRepository.save(department);
    }

    public Optional<Department> findByManagerId(Long managerId) {
        return departmentRepository.findByManagerUserId(managerId);
    }

} 