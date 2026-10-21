package com.aiopsengineservice.Entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CodePatchResponse {
    String rootCauseExplanation;
    String fixedMethodCode;
    String commitMessage;
}