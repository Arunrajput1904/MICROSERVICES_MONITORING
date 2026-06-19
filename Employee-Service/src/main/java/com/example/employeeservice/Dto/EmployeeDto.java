package com.example.employeeservice.Dto;

import jakarta.persistence.Entity;
import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class EmployeeDto {
    Long id;
    String name;
}
