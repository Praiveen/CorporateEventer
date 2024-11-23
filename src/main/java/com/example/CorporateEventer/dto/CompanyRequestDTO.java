package com.example.CorporateEventer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequestDTO {
    private Long companyId;
    private String message;
}
