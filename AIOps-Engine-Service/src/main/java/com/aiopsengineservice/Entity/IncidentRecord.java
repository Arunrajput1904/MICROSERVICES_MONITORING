package com.aiopsengineservice.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IncidentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hash;

    private String trace;

    private String resolution;

    public IncidentRecord(String hash, String trace, String resolution) {
        this.hash = hash;
        this.trace = trace;
        this.resolution = resolution;
    }
}
