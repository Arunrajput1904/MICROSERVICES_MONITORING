package com.example.addressservice.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter

public class AddressEntity {



    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String city;

    Long employeeId;
}
