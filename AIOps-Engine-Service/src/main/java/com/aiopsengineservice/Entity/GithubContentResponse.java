package com.aiopsengineservice.Entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GithubContentResponse {
    String content;
    String encoding;
    String sha;
}