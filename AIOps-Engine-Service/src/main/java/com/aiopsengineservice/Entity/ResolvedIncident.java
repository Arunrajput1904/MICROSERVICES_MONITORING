package com.aiopsengineservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResolvedIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String errorHash;

    @Column(nullable = false, length = 20)
    private String category; 

    @Column(nullable = false, length = 100)
    private String serviceName; 

    @Column(columnDefinition = "TEXT")
    private String resolutionData; 

    @Column(nullable = false, length = 50)
    private String resolutionStatus; 

    @Column(nullable = false)
    private Integer occurrenceCount = 1;

    @Column(nullable = false)
    private LocalDateTime firstSeenAt;

    @Column(nullable = false)
    private LocalDateTime lastSeenAt;

    @PrePersist
    protected void onCreate() {
        this.firstSeenAt = LocalDateTime.now();
        this.lastSeenAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastSeenAt = LocalDateTime.now();
    }
}