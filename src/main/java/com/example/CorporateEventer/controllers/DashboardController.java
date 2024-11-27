package com.example.CorporateEventer.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CorporateEventer.dto.CompanyRequestDTO;
import com.example.CorporateEventer.dto.DepartmentUpdateDTO;
import com.example.CorporateEventer.dto.ResponseDto;
import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.Department;
import com.example.CorporateEventer.entities.Notification;
import com.example.CorporateEventer.entities.Role;
import com.example.CorporateEventer.entities.SubDepartment;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.services.CompanyService;
import com.example.CorporateEventer.services.DepartmentService;
import com.example.CorporateEventer.services.NotificationService;
import com.example.CorporateEventer.services.RoleService;
import com.example.CorporateEventer.services.SubDepartmentService;
import com.example.CorporateEventer.services.UserService;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/dashboard")
@RestController
public class DashboardController {

    @Autowired
    private UserService userService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private SubDepartmentService subDepartmentService;
    @Autowired
    private RoleService roleService;


    /*
     * Создание новой компании
     */
    @PostMapping("/starter/createCompany/newcompany")
    public ResponseEntity<String> createCompany(@RequestBody Company company) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            currentUser = userService.findById(currentUser.getUserId().intValue())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    
            company.setUsers(new ArrayList<>());
            company.setDepartments(new ArrayList<>());
    
            company.setDirector(currentUser);
            Company savedCompany = companyService.save(company);
            // roleService.addRoleToUser(currentUser.getUserId(), Role.DIRECTOR);
            roleService.changeUserRole(currentUser.getUserId(), Role.USER, Role.DIRECTOR);

            currentUser.setCompany(savedCompany);
            userService.save(currentUser);
    
            savedCompany.getUsers().add(currentUser);
            companyService.save(savedCompany);
    
            
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /*
     * Загрузка листа компаний для selector на starter
     */
    @GetMapping("/companies")
    public ResponseEntity<List<Map<String, Object>>> getAllCompanies() {
        List<Company> companies = companyService.findAll();
        
        List<Map<String, Object>> companyDTOs = companies.stream()
            .map(company -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", company.getCompanyId());
                dto.put("name", company.getCompanyName());
                dto.put("address", company.getAddress());
                if (company.getDirector() != null) {
                    dto.put("directorId", company.getDirector().getUserId());
                    dto.put("directorName", company.getDirector().getFirstName() + " " + company.getDirector().getLastName());
                }
                return dto;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(companyDTOs);
    }

    /*
     * Запрос присоеденения к компании
     */
    @PostMapping("/companies/request")
    public ResponseEntity<?> createCompanyRequest(@RequestBody CompanyRequestDTO requestDTO) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            Optional<Company> companyOptional = companyService.findById(requestDTO.getCompanyId());
            if (!companyOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Компания не найдена");
            }
            Company company = companyOptional.get();
            if (notificationService.existsBySenderAndCompanyAndIsCompleted(currentUser, company, false)) {
                return ResponseEntity.badRequest().body("У вас уже есть активная заявка на вступление в эту компанию");
            }
            
            if (currentUser.getCompany() != null) {
                return ResponseEntity.badRequest().body("Вы уже состоите в компании");
            }
            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setCompany(company);
            notification.setReceiver(userService.findById(company.getDirector().getUserId().intValue()).get());
            notification.setMessage("Пользователь " + currentUser.getFirstName() + ' ' + currentUser.getLastName() + " хочет присоединиться к компании");
            notification.setType("actionMessage");
            notification.setSendDate(LocalDateTime.now());
            notification.setCompleted(false);
            
            notificationService.save(notification);
            return ResponseEntity.ok("Заявка успешно отправлена");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании заявки: " + e.getMessage());
        }
    }
    
    /*
     * Загрузка уведомлений на dashboard
     */
    @GetMapping("/received")
    public ResponseEntity<List<Map<String, Object>>> getReceivedNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        List<Notification> notifications = notificationService.findByReceiverAndIsCompleted(currentUser, false);
        
        List<Map<String, Object>> notificationDTOs = notifications.stream()
            .map(notification -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", notification.getNotificationId());
                dto.put("message", notification.getMessage());
                dto.put("senderName", notification.getSender().getUsername());
                dto.put("companyName", notification.getCompany().getCompanyName());
                dto.put("sendDate", notification.getSendDate().toString());
                dto.put("type", notification.getType());
                return dto;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(notificationDTOs);
    }

    /*
     * Прием заявки на вступление в компанию
     */
    @PostMapping("/notifications/accept/{notificationId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long notificationId) {
        try {
            Notification notification = notificationService.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

            if (notification.isCompleted()) {
                return ResponseEntity.badRequest().body(new ResponseDto("Заявка уже обработана", true));
            }

            User user = notification.getSender();
            Company company = notification.getCompany();
            user.setCompany(company);

            roleService.changeUserRole(user.getUserId(), Role.USER, Role.EMPLOYEE);

            userService.save(user);

            notification.setCompleted(true);
            notificationService.save(notification);

            // company.getUsers().add(user);
            // companyService.save(company);

            return ResponseEntity.ok(new ResponseDto("Заявка успешно принята", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseDto("Ошибка при принятии заявки: " + e.getMessage(), true));
        }
    }

    /*
     * Отклонение заявки на вступление в компанию
     */
    @PostMapping("/notifications/reject/{notificationId}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long notificationId) {
        try {
            Notification notification = notificationService.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));
            if (notification.isCompleted()) {
                return ResponseEntity.badRequest().body(new ResponseDto("Заявка уже обработана", true));
            }
            notification.setCompleted(true);


            Notification notificationAboutReject = new Notification();
            notificationAboutReject.setSender(notification.getReceiver());
            notificationAboutReject.setCompany(notification.getCompany());
            notificationAboutReject.setReceiver(notification.getSender());
            notificationAboutReject.setMessage("Пользователь " + notification.getReceiver().getFirstName() + ' ' + notification.getReceiver().getLastName() + " отклонил заявку");
            notificationAboutReject.setType("simpleMessage");
            notificationAboutReject.setSendDate(LocalDateTime.now());
            notificationAboutReject.setCompleted(false);

            notificationService.save(notification);
            notificationService.save(notificationAboutReject);

            return ResponseEntity.ok(new ResponseDto("Заявка успешно отклонена", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseDto("Ошибка при отклонении заявки: " + e.getMessage(), true));
        }
    }

    /*
     * Отметка о прочитанном уведомлении
     */
    @PostMapping("/notifications/read/{notificationId}")
    public ResponseEntity<?> readRequest(@PathVariable Long notificationId) {
        try {
            Notification notification = notificationService.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));
            if (notification.isCompleted()) {
                return ResponseEntity.badRequest().body(new ResponseDto("Заявка уже обработа��а", true));
            }
            notification.setCompleted(true);
            notificationService.save(notification);
            
            return ResponseEntity.ok(new ResponseDto("Уведомление помечено проч��танным", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseDto("Ошибка при отклонении заявки: " + e.getMessage(), true));
        }
    }


    /*
     * Загрузка пользователей компании на dashboard
     */
    @GetMapping("/company/users")
    public ResponseEntity<List<Map<String, Object>>> getCompanyUsers() {
        Authentication authentication = userService.userInfoFromSecurity();
        User currentUser = (User) authentication.getPrincipal();
        Company company = currentUser.getCompany();
        List<User> allUsers = company.getUsers();
        
        List<Map<String, Object>> availableUsers = allUsers.stream()
            .filter(user -> 
                !user.equals(company.getDirector()) && // Не директор
                !departmentService.isUserDepartmentManager(user) && // Не менеджер отдела
                !subDepartmentService.isUserSubDepartmentManager(user) && // Не менеджер подотдела
                user.getDepartment() == null && // Не состоит в отделе
                user.getSubDepartment() == null) // Не состоит в подотделе
            .map(user -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("userId", user.getUserId());
                dto.put("firstName", user.getFirstName());
                dto.put("lastName", user.getLastName());
                return dto;
            })
            .collect(Collectors.toList());

        System.out.println("ssssssssssssssssssssssssssssssssssssssssss");
        System.out.println(roleService.getUserRoles(currentUser.getUserId()));
        
        return ResponseEntity.ok(availableUsers);
    }

    /*
     * Создание отдела
     */
    @PostMapping("/departments/create")
    public ResponseEntity<?> createDepartment(@RequestBody Map<String, String> departmentData) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            if (!currentUser.equals(currentUser.getCompany().getDirector())) {
                return ResponseEntity.status(403).body("Только директор может создавать отделы");
            }
    
            Long headId = Long.parseLong(departmentData.get("headId"));
            User head = userService.findById(headId.intValue())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            if (head.getDepartment() != null || 
                head.getSubDepartment() != null || 
                departmentService.isUserDepartmentManager(head) || 
                subDepartmentService.isUserSubDepartmentManager(head)) {
                return ResponseEntity.badRequest()
                    .body("Выбранный пользователь уже является менеджером или состоит в другом отделе/подотделе");
            }
    
            Department department = new Department();
            department.setDepartmentName(departmentData.get("departmentName"));
            department.setCompany(currentUser.getCompany());
            department.setManager(head);

            roleService.changeUserRole(head.getUserId(), Role.EMPLOYEE, Role.DEPARTMENT_MANAGER);

            
            departmentService.save(department);
            userService.save(head);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании отдела: " + e.getMessage());
        }
    }

    /*
     * Загрузка отделов компании на dashboard
     */
    @GetMapping("/departments")
    public ResponseEntity<List<Map<String, Object>>> getDepartments() {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            Company company = currentUser.getCompany();
            List<Department> departments = departmentService.findByCompany(company);
            
            List<Map<String, Object>> departmentDTOs = departments.stream()
                .map(department -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", department.getDepartmentId());
                    dto.put("name", department.getDepartmentName());
                    dto.put("managerId", department.getManager().getUserId());
                    dto.put("managerName", 
                        department.getManager().getFirstName() + " " + 
                        department.getManager().getLastName());
                        dto.put("employeesCount", department.getUsers().size() + 1); 

                    return dto;
                })
                .collect(Collectors.toList());
                
            System.out.println(departmentDTOs);
            return ResponseEntity.ok(departmentDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

    /*
     * Удаление отдела
     */
    @PostMapping("/departments/delete/{departmentId}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long departmentId) {
        try {
            Department department = departmentService.findById(departmentId)
            .orElseThrow(() -> new RuntimeException("Отдел не найден"));
        
            roleService.changeUserRole(department.getManager().getUserId(), Role.DEPARTMENT_MANAGER, Role.EMPLOYEE);

            departmentService.deleteById(departmentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при удалении отдела: " + e.getMessage());
        }
    }

    /*
     * Получение данных отдела
     */
    @GetMapping("/departments/getdepatmentdata/{departmentId}")
    public ResponseEntity<?> getDepartment(@PathVariable Long departmentId) {
        try {
            Department department = departmentService.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Отдел не найден"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", department.getDepartmentId());
            response.put("name", department.getDepartmentName());
            response.put("managerId", department.getManager().getUserId());
            response.put("managerName", 
                department.getManager().getFirstName() + " " + 
                department.getManager().getLastName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Ошибка при получении данных отдела: " + e.getMessage());
        }
    }

    /*
     * Обновление отдела
     */
    @PostMapping("/departments/update/{departmentId}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long departmentId,
            @RequestBody DepartmentUpdateDTO updateDTO) {
        try {
            Department department = departmentService.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Отдел не найден"));
                
                if (updateDTO.getHeadId() != null) {
                    User newManager = userService.findById(updateDTO.getHeadId().intValue())
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
                    User oldManager = department.getManager();
                        
                    if (newManager.getDepartment() != null || 
                        newManager.getSubDepartment() != null || 
                        (departmentService.isUserDepartmentManager(newManager) && 
                         !department.getManager().equals(newManager)) || 
                        subDepartmentService.isUserSubDepartmentManager(newManager)) {
                        return ResponseEntity.badRequest()
                            .body("Выбранный пользователь уже является менеджером или состоит в другом отделе/подотделе");
                    }

                    roleService.changeUserRole(oldManager.getUserId(), Role.DEPARTMENT_MANAGER, Role.EMPLOYEE);
                    roleService.changeUserRole(newManager.getUserId(), Role.EMPLOYEE, Role.DEPARTMENT_MANAGER);
        
                
                
                    department.setManager(newManager);
                }
            
            if (updateDTO.getDepartmentName() != null) {
                department.setDepartmentName(updateDTO.getDepartmentName());
            }
            
            departmentService.save(department);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Ошибка при обновлении отдела: " + e.getMessage());
        }
    }

    /*
     * раздел подотделов
     */

    /*
    * Создание подотдела
    */
    @PostMapping("/departments/subdepartments/create")
    public ResponseEntity<?> createSubDepartment(@RequestBody Map<String, String> subdepartmentData) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            if (!currentUser.equals(currentUser.getCompany().getDirector()) && 
                !departmentService.isUserDepartmentManager(currentUser)) {
                return ResponseEntity.status(403).body("Только директор или руководитель отдела может создавать подотделы");
            }

            Long departmentId = Long.parseLong(subdepartmentData.get("departmentId"));
            Department parentDepartment = departmentService.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Отдел не найден"));

            Long headId = Long.parseLong(subdepartmentData.get("headId"));
            User head = userService.findById(headId.intValue())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
                

            if (!parentDepartment.equals(head.getDepartment())) {
                return ResponseEntity.badRequest()
                    .body("Выбранный пользователь должен быть сотрудником этого отдела");
            }
            
            if (subDepartmentService.isUserSubDepartmentManager(head) || 
                head.getSubDepartment() != null) {
                return ResponseEntity.badRequest()
                    .body("Выбранный пользователь уже является менеджером или состоит в другом подотделе");
            }
    
            SubDepartment subdepartment = new SubDepartment();
            subdepartment.setSubdepartmentName(subdepartmentData.get("subdepartmentName"));
            subdepartment.setDepartment(parentDepartment);
            subdepartment.setManager(head);

            roleService.changeUserRole(head.getUserId(), Role.EMPLOYEE, Role.SUBDEPARTMENT_MANAGER);
            
            subDepartmentService.save(subdepartment);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании подотдела: " + e.getMessage());
        }
    }



    /*
     * Загрузка подотделов компании на dashboard
     */
    @GetMapping("/departments/{departmentId}/subdepartments")
    public ResponseEntity<List<Map<String, Object>>> getSubDepartments(@PathVariable Long departmentId) {
        try {
            Department department = departmentService.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Отдел не найден"));
                
            List<Map<String, Object>> subdepartmentDTOs = department.getSubDepartments().stream()
                .map(subdepartment -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", subdepartment.getSubdepartmentId());
                    dto.put("name", subdepartment.getSubdepartmentName());
                    dto.put("departmentId", department.getDepartmentId());
                    dto.put("departmentName", department.getDepartmentName());
                    dto.put("managerId", subdepartment.getManager().getUserId());
                    dto.put("managerName", 
                        subdepartment.getManager().getFirstName() + " " + 
                        subdepartment.getManager().getLastName());
                    dto.put("employeesCount", subdepartment.getUsers().size() + 1); 

                    return dto;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(subdepartmentDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

    /*
     * Получение списка доступных руководителей подотделов
     */
    @GetMapping("/departments/{departmentId}/available-managers")
    public ResponseEntity<List<Map<String, Object>>> getAvailableSubDepartmentManagers(@PathVariable Long departmentId) {
        try {
            Department department = departmentService.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Отдел не найден"));
            
            List<User> availableManagers = department.getUsers().stream()
                .filter(user -> 
                    !user.equals(department.getManager()) && // Не является руководителем этого отдела
                    user.getSubDepartment() == null && // Не состоит в подотделе
                    !subDepartmentService.isUserSubDepartmentManager(user) // Не является руководителем подотдела
                )
                .collect(Collectors.toList());
    
            return ResponseEntity.ok(availableManagers.stream()
                .map(user -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("userId", user.getUserId());
                    dto.put("firstName", user.getFirstName());
                    dto.put("lastName", user.getLastName());
                    return dto;
                })
                .collect(Collectors.toList()));
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /*
    * Получение данных подотдела
    */
    @GetMapping("/departments/subdepartments/{subdepartmentId}")
    public ResponseEntity<?> getSubDepartment(@PathVariable Long subdepartmentId) {
        try {
            SubDepartment subdepartment = subDepartmentService.findById(subdepartmentId)
                .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", subdepartment.getSubdepartmentId());
            response.put("name", subdepartment.getSubdepartmentName());
            response.put("departmentId", subdepartment.getDepartment().getDepartmentId());
            response.put("managerId", subdepartment.getManager().getUserId());
            response.put("managerName", 
                subdepartment.getManager().getFirstName() + " " + 
                subdepartment.getManager().getLastName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Ошибка при получении данных подотдела: " + e.getMessage());
        }
    }

    /*
    * Обновление подотдела
    */
    @PostMapping("/departments/subdepartments/update/{subdepartmentId}")
    public ResponseEntity<?> updateSubDepartment(
            @PathVariable Long subdepartmentId,
            @RequestBody Map<String, String> updateData) {
        try {
            SubDepartment subdepartment = subDepartmentService.findById(subdepartmentId)
                .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
                
            subdepartment.setSubdepartmentName(updateData.get("subdepartmentName"));
            
            if (updateData.containsKey("headId")) {
                Long headId = Long.parseLong(updateData.get("headId"));
                User newManager = userService.findById(headId.intValue())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

                roleService.changeUserRole(subdepartment.getManager().getUserId(), Role.SUBDEPARTMENT_MANAGER, Role.EMPLOYEE);
                roleService.changeUserRole(newManager.getUserId(), Role.EMPLOYEE, Role.SUBDEPARTMENT_MANAGER);
        
                subdepartment.setManager(newManager);
            }
            
            subDepartmentService.save(subdepartment);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Ошибка при обновлении подотдела: " + e.getMessage());
        }
    }

    /*
    * Удаление подотдела
    */
    @PostMapping("/departments/subdepartments/delete/{subdepartmentId}")
    public ResponseEntity<?> deleteSubDepartment(@PathVariable Long subdepartmentId) {
        try {
            SubDepartment subdepartment = subDepartmentService.findById(subdepartmentId)
                .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
        
            roleService.changeUserRole(subdepartment.getManager().getUserId(), Role.SUBDEPARTMENT_MANAGER, Role.EMPLOYEE);
            
            subDepartmentService.deleteById(subdepartmentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Ошибка при удалении подотдела: " + e.getMessage());
        }
    }


    /*
     * Получение списка доступных сотрудников
     */
    @GetMapping("/employees/available/{targetType}/{targetId}")
    public ResponseEntity<List<Map<String, Object>>> getAvailableEmployees(
            @PathVariable String targetType,
            @PathVariable Long targetId) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            List<User> availableUsers = new ArrayList<>();

            if (targetType.equals("department")) {
                Department department = departmentService.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Отдел не найден"));
                
                if (!currentUser.equals(currentUser.getCompany().getDirector())) {
                    return ResponseEntity.status(403).body(null);
                }
                
                availableUsers = currentUser.getCompany().getUsers().stream()
                .filter(user -> user.getDepartment() == null && 
                               user.getSubDepartment() == null &&
                               !user.equals(currentUser.getCompany().getDirector()) &&
                               !departmentService.isUserDepartmentManager(user) &&
                               !subDepartmentService.isUserSubDepartmentManager(user))
                .collect(Collectors.toList());

            } else if (targetType.equals("subdepartment")) {
                SubDepartment subdepartment = subDepartmentService.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
                
                if (!currentUser.equals(currentUser.getCompany().getDirector()) && 
                    !currentUser.equals(subdepartment.getDepartment().getManager()) &&
                    !currentUser.equals(subdepartment.getManager())) {
                    return ResponseEntity.status(403).body(null);
                }
                
                availableUsers = subdepartment.getDepartment().getUsers().stream()
                .filter(user -> 
                    user.getSubDepartment() == null && // Не состоит в подотделе
                    !user.equals(subdepartment.getManager()) && // Не является менеджером этого подотдела
                    !user.equals(subdepartment.getDepartment().getManager()) && // Не является менеджером отдела
                    !subDepartmentService.isUserSubDepartmentManager(user) // Не является менеджером другого подотдела
                )
                .collect(Collectors.toList());
            }

            return ResponseEntity.ok(availableUsers.stream()
                .map(user -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("userId", user.getUserId());
                    dto.put("firstName", user.getFirstName());
                    dto.put("lastName", user.getLastName());
                    return dto;
                })
                .collect(Collectors.toList()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /*
     * Назначение сотрудников
     */
    @PostMapping("/employees/assign/{targetType}/{targetId}")
    public ResponseEntity<?> assignEmployees(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestBody List<Long> employeeIds) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
    
            if (targetType.equals("department")) {
                Department department = departmentService.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Отдел не найден"));
                
                if (!currentUser.equals(currentUser.getCompany().getDirector())) {
                    return ResponseEntity.status(403).body("Недостаточно прав");
                }
    
                for (Long employeeId : employeeIds) {
                    User employee = userService.findById(employeeId.intValue())
                        .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));
                    
                    employee.setDepartment(department);
                    department.getUsers().add(employee);
                    
                    userService.save(employee);
                }
                departmentService.save(department);
    
            } else if (targetType.equals("subdepartment")) {
                SubDepartment subdepartment = subDepartmentService.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
                
                if (!currentUser.equals(currentUser.getCompany().getDirector()) && 
                    !currentUser.getUserId().equals(subdepartment.getDepartment().getManager().getUserId()) &&
                    !currentUser.getUserId().equals(subdepartment.getManager().getUserId())) {
                    return ResponseEntity.status(403).body("Недостаточно прав");
                }
    
                for (Long employeeId : employeeIds) {
                    User employee = userService.findById(employeeId.intValue())
                        .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));
                    
                    employee.setDepartment(subdepartment.getDepartment());
                    employee.setSubDepartment(subdepartment);
                    subdepartment.getUsers().add(employee);
                    subdepartment.getDepartment().getUsers().add(employee);
                    
                    userService.save(employee);
                }
                subDepartmentService.save(subdepartment);
                departmentService.save(subdepartment.getDepartment());
            }
    
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при назначении сотрудников: " + e.getMessage());
        }
    }

    /*
     * Получение списка сотрудников отдела
     */
    @GetMapping("/departments/{departmentId}/employees")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentEmployees(@PathVariable Long departmentId) {
        try {
            Department department = departmentService.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Отдел не найден"));
            
            List<Map<String, Object>> employeeDTOs = department.getUsers().stream()
                .map(user -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("userId", user.getUserId());
                    dto.put("firstName", user.getFirstName());
                    dto.put("lastName", user.getLastName());
                    dto.put("subdepartmentId", user.getSubDepartment() != null ? 
                        user.getSubDepartment().getSubdepartmentId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(employeeDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /*
     * Получение списка сотрудников подотдела
     */
    @GetMapping("/subdepartments/{subdepartmentId}/employees")
    public ResponseEntity<List<Map<String, Object>>> getSubDepartmentEmployees(@PathVariable Long subdepartmentId) {
        try {
            SubDepartment subdepartment = subDepartmentService.findById(subdepartmentId)
                .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
            
            List<Map<String, Object>> employeeDTOs = subdepartment.getUsers().stream()
                .map(user -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("userId", user.getUserId());
                    dto.put("firstName", user.getFirstName());
                    dto.put("lastName", user.getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(employeeDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /*
     * Удаление сотрудника из отдела/подотдела
     */
    @PostMapping("/employees/remove/{targetType}/{targetId}")
    public ResponseEntity<?> removeEmployees(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestBody List<Long> employeeIds) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
    
            if (targetType.equals("department")) {
                Department department = departmentService.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Отдел не найден"));
                
                if (!currentUser.equals(currentUser.getCompany().getDirector())) {
                    return ResponseEntity.status(403).body("Недостаточно прав");
                }
    
                for (Long employeeId : employeeIds) {
                    User employee = userService.findById(employeeId.intValue())
                        .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));
                    
                    if (subDepartmentService.isUserSubDepartmentManager(employee)) {
                        return ResponseEntity.badRequest()
                            .body("Нельзя удалить руководителя подотдела из отдела");
                    }
                    
                    employee.setDepartment(null);
                    employee.setSubDepartment(null);
                    department.getUsers().remove(employee);
                    
                    userService.save(employee);
                }
                departmentService.save(department);
    
            } else if (targetType.equals("subdepartment")) {
                SubDepartment subdepartment = subDepartmentService.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
                
                if (!currentUser.equals(currentUser.getCompany().getDirector()) && 
                    !currentUser.getUserId().equals(subdepartment.getDepartment().getManager().getUserId()) &&
                    !currentUser.getUserId().equals(subdepartment.getManager().getUserId())) {
                    return ResponseEntity.status(403).body("Недостаточно прав");
                }
    
                for (Long employeeId : employeeIds) {
                    User employee = userService.findById(employeeId.intValue())
                        .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));
                    
                    employee.setSubDepartment(null);
                    subdepartment.getUsers().remove(employee);
                    
                    userService.save(employee);
                }
                subDepartmentService.save(subdepartment);
            }
    
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при удалении сотрудников: " + e.getMessage());
        }
    }


    @PostMapping("/access/grant-full/{userId}")
    public ResponseEntity<?> grantFullAccess(@PathVariable Long userId) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            if (!currentUser.hasRole(Role.DIRECTOR)) {
                return ResponseEntity.status(403).body("Только директор может назначать п��лный доступ");
            }

        roleService.changeUserRole(userId, null, Role.FULL_ACCESS);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при назначении полного доступа: " + e.getMessage());
        }
    }

    /*
     * Отзыв полного доступа
     */
    @PostMapping("/access/revoke-full/{userId}")
    public ResponseEntity<?> revokeFullAccess(@PathVariable Long userId) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            if (!currentUser.hasRole(Role.DIRECTOR)) {
                return ResponseEntity.status(403).body("Только директор может отзывать полный доступ");
            }

        roleService.changeUserRole(userId, Role.FULL_ACCESS, null);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при отзыве полного доступа: " + e.getMessage());
        }
    }
    


    
}
