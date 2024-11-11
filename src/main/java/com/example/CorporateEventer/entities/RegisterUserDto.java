package com.example.CorporateEventer.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterUserDto {
    private String email;
    
    private String password;
    
    private String name;

    public String getName() {
        return name;
    }
}
