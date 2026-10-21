package com.aiopsengineservice.Entity;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class IncidentClassificationDto {
    String category;
    String problemType;
    String serviceName;
    String rawSummary ;
    String targetClass;
    int lineNumber;

}
