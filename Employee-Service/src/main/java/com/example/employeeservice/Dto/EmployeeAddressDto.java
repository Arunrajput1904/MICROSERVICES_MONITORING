package com.example.employeeservice.Dto;


import jakarta.persistence.Entity;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class EmployeeAddressDto {

    String name;
    String city;

}
