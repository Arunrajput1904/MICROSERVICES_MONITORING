package com.aiopsengineservice.Service;

import com.aiopsengineservice.Entity.GithubContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class GitHubApiService {

    private final String owner = "Arunrajput1904";
    private final String repo = "MICROSERVICES_MONITORING";
    private final RestClient client;

    public GitHubApiService(RestClient.Builder client , @Value("${pat}") String token) {
        this.client = client
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization","Bearer "+token)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    public String fetchMethodCode(String serviceName,String targetClass, String methodName, int lineNumber) {
log.info("fetch of file");

        String filepath ="%s/src/main/java/%s.java".formatted(serviceName,targetClass.replace(".","/"));

        log.info("fetch success of file"+"  ----   "+filepath);

        String uri = UriComponentsBuilder.fromPath("/repos/{owner}/{repo}/contents/" + filepath)
                .buildAndExpand(owner, repo)
                .toUriString();


        GithubContentResponse response=  client.get()
                .uri(uri)
                .retrieve()
                .body(GithubContentResponse.class);

        if (response == null || response.getContent() == null) {
            throw new RuntimeException("Could not find file at path: " + filepath);
        }

        log.info("whole file fetched from  repo");

        String cleanBase64 = response.getContent().replaceAll("\\s", "");
        byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);
        String fullFileCode = new String(decodedBytes, StandardCharsets.UTF_8);



        log.info(" file send back to tool");

        return  fullFileCode;


    }

    private String givethedesiredContent(String fullFileCode, int lineNumber) {

        String[] lines = fullFileCode.split("\n");
        int startLine = Math.max(0, lineNumber - 30);
        int endLine = Math.min(lines.length - 1, lineNumber + 30);
        StringBuilder snippet = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            snippet.append(String.format("%04d: %s%n", i + 1, lines[i]));
        }
        return snippet.toString();
    }

    public String createPullRequestWithPatch(
            String targetClass,
            String branchName,
            String codepush,
            String codemessage,
            String serviceName) {

        log.info("Starting PR creation process for class: {}", branchName+" ......."+codepush);

        String filepath = "%s/src/main/java/%s.java".formatted(
                serviceName,
                targetClass.replace(".", "/")
        );

        Map<?, ?> mainRef = client.get()
                .uri("/repos/{owner}/{repo}/git/ref/heads/main", owner, repo)
                .retrieve()
                .body(Map.class);

        if (mainRef == null || !mainRef.containsKey("object")) {
            throw new IllegalStateException("Failed to fetch main branch reference from GitHub.");
        }

        Map<?, ?> objectMap = (Map<?, ?>) mainRef.get("object");
        String latestCommitSha = (String) objectMap.get("sha");
        log.info("Latest commit SHA on main: {}", latestCommitSha);

        try {
            client.post()
                    .uri("/repos/{owner}/{repo}/git/refs", owner, repo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "ref", "refs/heads/" + branchName,
                            "sha", latestCommitSha
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Branch '{}' created successfully.", branchName);
        } catch (Exception e) {
            log.error("Failed to create branch '{}'. It may already exist.", branchName, e);
        }

        GithubContentResponse fileContent = client.get()
                .uri("/repos/{owner}/{repo}/contents/{path}?ref={ref}", owner, repo, filepath, branchName)
                .retrieve()
                .body(GithubContentResponse.class);

        if (fileContent == null || fileContent.getSha() == null) {
            throw new IllegalStateException("Target file SHA could not be fetched at path: " + filepath);
        }

        String encodedNewCode = Base64.getEncoder().encodeToString(codepush.getBytes(StandardCharsets.UTF_8));

        client.put()
                .uri("/repos/{owner}/{repo}/contents/{path}", owner, repo, filepath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "message", codemessage,
                        "content", encodedNewCode,
                        "sha", fileContent.getSha(),
                        "branch", branchName
                ))
                .retrieve()
                .toBodilessEntity();

        log.info("Updated file committed to branch '{}'.", branchName);

        Map<?, ?> prResponse = client.post()
                .uri("/repos/{owner}/{repo}/pulls", owner, repo)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "title", "AIOps Auto-Fix: " + targetClass,
                        "head", branchName,
                        "base", "main",
                        "body", "Automated fix generated by AIOps Coder/Validator engine."
                ))
                .retrieve()
                .body(Map.class);

        if (prResponse == null || !prResponse.containsKey("html_url")) {
            throw new IllegalStateException("Pull request created but no html_url returned.");
        }

        String prUrl = (String) prResponse.get("html_url");
        log.info("Pull request successfully created: {}", prUrl);

        return prUrl;
    }
}
