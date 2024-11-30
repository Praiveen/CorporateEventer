package com.example.CorporateEventer.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.CorporateEventer.entities.Company;
import com.example.CorporateEventer.entities.User;
import com.example.CorporateEventer.services.CompanyService;
import com.example.CorporateEventer.services.UserService;



@Controller
public class PageController {
    
    @Autowired
    private UserService userService;
    @Autowired
    private CompanyService companyService;

    @GetMapping("/")
    public String index1() {
        return "index";
    }

    // @GetMapping("/")
    // public String index2() {
    //     return "index";
    // }

    @GetMapping("/register")
    public String registration() {
        if (userService.userInfoFromSecurity().getPrincipal().equals("anonymousUser")) {
            return "/register";
        }
        return "redirect:/dashboard";
    }



    @GetMapping("/login")
    public String loginPage() {
        if (!userService.userInfoFromSecurity().getPrincipal().equals("anonymousUser")) {
            System.out.println("1111");
            return "redirect:dashboard";
        }
        System.out.println("fff");
        return "login";
    }

    @GetMapping("/profile")
    public String userPageLoader() {
        return "profile";
    }

    @GetMapping("/dashboard/starter")
    public String starterLoader() {
        if(userService.userInfoFromSecurity().getPrincipal().equals("anonymousUser")){
            return "redirect:/login";
        }
        Authentication authentication = userService.userInfoFromSecurity();
        User currentUser = (User) authentication.getPrincipal();
        if (currentUser.getCompany() != null)
            return "redirect:/dashboard";

        return "starter";
    }

    @GetMapping("/dashboard/starter/createCompany")
    public String createCompanyrLoader() {
        Authentication authentication = userService.userInfoFromSecurity();
        User currentUser = (User) authentication.getPrincipal();
        if (currentUser.getCompany() != null)
            return "redirect:/dashboard";
        return "createCompany";
    }

    @GetMapping("/dashboard")
    public String dashboardLoader(Model model) {
        if(userService.userInfoFromSecurity().getPrincipal().equals("anonymousUser")){
            return "redirect:/login";
        }
        Authentication authentication = userService.userInfoFromSecurity();
        User currentUser = (User) authentication.getPrincipal();
        if (currentUser.getCompany() == null)
            return "redirect:/dashboard/starter";
        Company company = companyService.findByDirector(currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("company", company);
        return "dashboard";
    }


}

