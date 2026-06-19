package com.example.addressservice.Dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddressDto {
    Long id;
    String city;
    Long employeeId;
}
