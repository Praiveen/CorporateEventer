package com.example.CorporateEventer.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.Department;
import com.example.CorporateEventer.entities.Event;
import com.example.CorporateEventer.entities.Meeting;
import com.example.CorporateEventer.entities.Notification;
import com.example.CorporateEventer.entities.SubDepartment;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.services.CompanyService;
import com.example.CorporateEventer.services.DepartmentService;
import com.example.CorporateEventer.services.EventService;
import com.example.CorporateEventer.services.MeetingService;
import com.example.CorporateEventer.services.NotificationService;
import com.example.CorporateEventer.services.SubDepartmentService;
import com.example.CorporateEventer.services.UserService;

@RequestMapping("/event")
@RestController
public class EventController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private SubDepartmentService subDepartmentService;

    @Autowired
    private EventService eventService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private NotificationService notificationService;



    /*
     * Лист отделов и подотделов для создания мероприятия и события
     */
    @GetMapping("/available-recipients")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getAvailableRecipients() {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            Map<String, List<Map<String, Object>>> response = new HashMap<>();
            List<Map<String, Object>> departments = new ArrayList<>();
            List<Map<String, Object>> subdepartments = new ArrayList<>();

            if (currentUser.equals(currentUser.getCompany().getDirector())) {
                List<Department> allDepartments = departmentService.findByCompany(currentUser.getCompany());
                for (Department dept : allDepartments) {
                    Map<String, Object> deptMap = new HashMap<>();
                    deptMap.put("id", dept.getDepartmentId());
                    deptMap.put("name", dept.getDepartmentName());
                    departments.add(deptMap);
            
                    for (SubDepartment subDept : dept.getSubDepartments()) {
                        Map<String, Object> subDeptMap = new HashMap<>();
                        subDeptMap.put("id", subDept.getSubdepartmentId());
                        subDeptMap.put("name", subDept.getSubdepartmentName());
                        subdepartments.add(subDeptMap);
                    }
                }
            }
            else if (departmentService.isUserDepartmentManager(currentUser)) {
                Department userDepartment = departmentService.findByManagerId(currentUser.getUserId().longValue()).get();
                if (userDepartment != null) {
                    Map<String, Object> deptMap = new HashMap<>();
                    deptMap.put("id", userDepartment.getDepartmentId());
                    deptMap.put("name", userDepartment.getDepartmentName());
                    departments.add(deptMap);
            
                    for (SubDepartment subDept : userDepartment.getSubDepartments()){
                        Map<String, Object> subDeptMap = new HashMap<>();
                        subDeptMap.put("id", subDept.getSubdepartmentId());
                        subDeptMap.put("name", subDept.getSubdepartmentName());
                        subdepartments.add(subDeptMap);
                    }
                }
            }
            else if (subDepartmentService.isUserSubDepartmentManager(currentUser)) {
                SubDepartment userSubDepartment = subDepartmentService.findByManager(currentUser);
                if (userSubDepartment != null) {
                    Map<String, Object> subDeptMap = new HashMap<>();
                    subDeptMap.put("id", userSubDepartment.getSubdepartmentId());
                    subDeptMap.put("name", userSubDepartment.getSubdepartmentName());
                    subdepartments.add(subDeptMap);
                }
            }

            response.put("departments", departments);
            response.put("subdepartments", subdepartments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/createEvent/{type}/{id}")
    public ResponseEntity<?> createEvent(@RequestBody Event event, 
                                    @PathVariable String type, 
                                    @PathVariable Long id) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            event.setCreatedBy(currentUser);
            event.setStatus("PLANNED");
            List<User> participants = new ArrayList<>();
            System.out.println(event);
            
            if ("department".equals(type)) {
                Department dept = departmentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Отдел не найден"));
                participants.addAll(dept.getUsers());
                if (dept.getManager() != null) {
                    participants.add(dept.getManager());
                }
            } 
            else if ("subdepartment".equals(type)) {
                SubDepartment subDept = subDepartmentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
                participants.addAll(subDept.getUsers());
                if (subDept.getManager() != null) {
                    participants.add(subDept.getManager());
                }
            }
            event.setParticipants(participants);
            for (User participant : participants) {
                Notification notification = new Notification();
                notification.setSender(event.getCreatedBy());
                notification.setReceiver(participant);
                notification.setCompany(event.getCreatedBy().getCompany());
                notification.setMessage("Вы добавлены в событие: " + event.getTitle());
                notification.setType("simpleMessage");
                notification.setSendDate(LocalDateTime.now());
                notification.setCompleted(false);
                notificationService.save(notification);
            }
            eventService.save(event);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании события: " + e.getMessage());
        }
    }
    
    @GetMapping("/user-events")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getUserEvents() {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            LocalDateTime now = LocalDateTime.now();
            List<Event> allEvents = eventService.findByParticipant(currentUser);
            
            List<Map<String, Object>> currentEvents = allEvents.stream()
                .filter(event -> event.getEndTime().isAfter(now))
                .sorted(Comparator.comparing(Event::getStartTime))
                .map(event -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("eventId", event.getEventId());
                    dto.put("title", event.getTitle());
                    dto.put("description", event.getDescription());
                    dto.put("startTime", event.getStartTime().toString());
                    dto.put("endTime", event.getEndTime().toString());
                    dto.put("location", event.getLocation());
                    dto.put("status", event.getStatus());
                    dto.put("createdBy", event.getCreatedBy().getFirstName() + " " + event.getCreatedBy().getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
                
            List<Map<String, Object>> pastEvents = allEvents.stream()
                .filter(event -> event.getEndTime().isBefore(now))
                .sorted(Comparator.comparing(Event::getStartTime).reversed())
                .map(event -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("eventId", event.getEventId());
                    dto.put("title", event.getTitle());
                    dto.put("description", event.getDescription());
                    dto.put("startTime", event.getStartTime().toString());
                    dto.put("endTime", event.getEndTime().toString());
                    dto.put("location", event.getLocation());
                    dto.put("status", event.getStatus());
                    dto.put("createdBy", event.getCreatedBy().getFirstName() + " " + event.getCreatedBy().getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
            
            Map<String, List<Map<String, Object>>> response = new HashMap<>();
            response.put("currentEvents", currentEvents);
            response.put("pastEvents", pastEvents);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }



    @PostMapping("/createMeeting/{type}/{id}")
    public ResponseEntity<?> createMeeting(@RequestBody Meeting meeting, 
                                    @PathVariable String type, 
                                    @PathVariable Long id) {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            meeting.setOrganizer(currentUser);
            meeting.setStatus("PLANNED");
            
            List<User> participants = new ArrayList<>();
            System.out.println(meeting);
            
            if ("department".equals(type)) {
                Department dept = departmentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Отдел не найден"));
                participants.addAll(dept.getUsers());
                if (dept.getManager() != null) {
                    participants.add(dept.getManager());
                }
            } 
            else if ("subdepartment".equals(type)) {
                SubDepartment subDept = subDepartmentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Подотдел не найден"));
                participants.addAll(subDept.getUsers());
                if (subDept.getManager() != null) {
                    participants.add(subDept.getManager());
                }
            }
            
            meeting.setParticipants(participants);

            for (User participant : participants) {
                Notification notification = new Notification();
                notification.setSender(meeting.getOrganizer());
                notification.setReceiver(participant);
                notification.setCompany(meeting.getOrganizer().getCompany());
                notification.setMessage("Вы добавлены в событие: " + meeting.getTopic());
                notification.setType("simpleMessage");
                notification.setSendDate(LocalDateTime.now());
                notification.setCompleted(false);
                
                notificationService.save(notification);
            }

            meetingService.save(meeting);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании события: " + e.getMessage());
        }
    }


    @GetMapping("/user-meetings")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getUserMeeting() {
        try {
            Authentication authentication = userService.userInfoFromSecurity();
            User currentUser = (User) authentication.getPrincipal();
            
            LocalDateTime now = LocalDateTime.now();
            List<Meeting> allMeetings = meetingService.findByParticipant(currentUser);
            
            List<Map<String, Object>> currentMeetings = allMeetings.stream()
                .filter(meeting -> meeting.getEndTime().isAfter(now))
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .map(meeting -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("meetingId", meeting.getMeetingId());
                    dto.put("title", meeting.getTopic());
                    dto.put("description", meeting.getAgenda());
                    dto.put("startTime", meeting.getStartTime().toString());
                    dto.put("endTime", meeting.getEndTime().toString());
                    // dto.put("location", meeting.getLocation());
                    dto.put("status", meeting.getStatus());
                    dto.put("createdBy", meeting.getOrganizer().getFirstName() + " " + meeting.getOrganizer().getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
                
            List<Map<String, Object>> pastMeetings = allMeetings.stream()
                .filter(meeting -> meeting.getEndTime().isBefore(now))
                .sorted(Comparator.comparing(Meeting::getStartTime).reversed())
                .map(meeting -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("eventId", meeting.getMeetingId());
                    dto.put("title", meeting.getTopic());
                    dto.put("description", meeting.getAgenda());
                    dto.put("startTime", meeting.getStartTime().toString());
                    dto.put("endTime", meeting.getEndTime().toString());
                    // dto.put("location", meeting.getLocation());
                    dto.put("status", meeting.getStatus());
                    dto.put("createdBy", meeting.getOrganizer().getFirstName() + " " + meeting.getOrganizer().getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
            
            Map<String, List<Map<String, Object>>> response = new HashMap<>();
            response.put("currentEvents", currentMeetings);
            response.put("pastEvents", pastMeetings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }


}
