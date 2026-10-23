package com.aiopsengineservice.Service;


import com.aiopsengineservice.Entity.CodePatchResponse;
import com.aiopsengineservice.Entity.IncidentClassificationDto;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.internal.util.StringHelper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Slf4j
@Service
public class IncidentResolvedServiceProvider {

    private final ChatClient chatClient;
   private final SimilarityFind similarityFind;
   private final GitHubApiService githubApiService;
   private final PersistenceService persistenceService;
   private final InfraResultService resultService;


    public IncidentResolvedServiceProvider(ChatClient.Builder chatClient, SimilarityFind similarityFind, GitHubApiService githubApiService, PersistenceService persistenceService, InfraResultService resultService) {
        this.chatClient = chatClient.build();
        this.similarityFind = similarityFind;
        this.githubApiService = githubApiService;
        this.persistenceService = persistenceService;
        this.resultService = resultService;
    }

    @Tool(description = "Use this tool for application code bugs to fetch code, fix it via Coder/Validator agents, open a PR, and persist the result.")
    public String triggerGitHubAutoFix(@ToolParam(description ="Exact microservice folder name. " +
                                                   "MUST be one of: 'Address-Service', 'AIOps-Engine-Service', 'Api-Gateway', 'Employee-Service', 'config-server', 'emailservice', 'serviceDiscovery'")
                                           String serviceName,
                                       @ToolParam(description = "The fully qualified class name extracted from the stack trace") String targetClass,
                                       @ToolParam(description = "The exact line number where the exception occurred") int lineNumber,
                                       @ToolParam(description = "Give the name of method is very near to error ") String methodName,
                                       @ToolParam(description = "Summary of error , like type ") String summary,
                                       ToolContext context) {

log.info("Code review or correction Starts");
        String rawJavaFileCode = githubApiService.fetchMethodCode(serviceName, targetClass, methodName, lineNumber);

        CompilationUnit astTree = StaticJavaParser.parse(rawJavaFileCode);
        String sanitizedLog = (String) context.getContext().get("normalizedTrace");


        MethodDeclaration originalMethodNode = astTree.findAll(MethodDeclaration.class).stream()
                .filter(method -> method.getNameAsString().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Method '" + methodName + "' not found in AST."));


        StringBuilder classDependencies = new StringBuilder();
        astTree.findAll(FieldDeclaration.class).forEach(field ->
                classDependencies.append(field.toString()).append("\n")
        );

        String coderPrompt = """
            You are an expert Java Spring Boot Automated Patching Agent.
            
            FAILING SERVICE: %s
            TARGET CLASS: %s
            FAILING METHOD NAME: %s
            LINE NUMBER: %d
            
            AVAILABLE CLASS DEPENDENCIES (Fields & Repositories):
            ```java
            %s
            ```
            
            ORIGINAL STACK TRACE:
            %s
            
            FAILING METHOD SOURCE CODE:
            ```java
            %s
            ```
            
            STRICT FIXING RULES:
            1. FIX ONLY THE FAILING METHOD: Do not modify method signatures unless required.
            2. DOMAIN ENTITY STRICTNESS: Do not invent getters, setters, or fields on custom domain objects (e.g., entity or DTO classes). If you use a class repository, use ONLY the exact field names provided in AVAILABLE CLASS DEPENDENCIES.
            3. SPRING DATA JPA: Always handle Optional when using findById(). Use `.orElse(null)` or `.orElseThrow()`.
            4. COMPLETE CODE: Return the FULL, compilable Java method.
            """.formatted(serviceName, targetClass, methodName, lineNumber, classDependencies.toString(), sanitizedLog, originalMethodNode.toString());

log.info("File correction starts");
        CodePatchResponse result = chatClient.prompt()
                .user(coderPrompt)
                .call()
                .entity(CodePatchResponse.class);
        log.info("File correction end");


        log.info(result+"  ****");

        log.info("File validation starts");
        String validatorPrompt = String.format("""
            You are an expert, strict Java Code Reviewer and Automated Patching Agent.
            
            INPUT CONTEXT:
            Original Error: %s
            Original Method Code: %s
            Proposed Patch: %s
            
            STRICT GROUNDING RULES:
            1. Adhere Strictly to Original Code: Do NOT invent non-existent variables or classes.
            2. Method Signature Integrity: Keep method name, annotations, and parameters identical.
            3. Complete Implementation: Output must be fully compilable.
            
            OUTPUT REQUIREMENTS:
            - Output MUST be a raw JSON object.
            - Do NOT wrap the JSON in markdown blocks (no ```json).
            - Do NOT wrap the fixedMethodCode inside markdown code blocks (no ```java inside the JSON string).
            """, sanitizedLog, originalMethodNode.toString(), result.getFixedMethodCode());


        CodePatchResponse validatedPatch = chatClient.prompt()
                .user(validatorPrompt)
                .call()
                .entity(CodePatchResponse.class);

        log.info("File validation end ............................................."+validatedPatch);

        String codepush=validatedPatch.getFixedMethodCode();
        String cleanCodePush = codepush.replaceAll("```java\\n?", "").replaceAll("```\\n?", "").trim();
        String codemessage=validatedPatch.getCommitMessage();

       MethodDeclaration message= StaticJavaParser.parseMethodDeclaration(cleanCodePush);
       originalMethodNode.replace(message);
        String errorHash= similarityFind.generateSha256(sanitizedLog);
        String branchName = "aiops-fix-" + UUID.randomUUID().toString().substring(0, 8);

        String prUrl = githubApiService.createPullRequestWithPatch(targetClass, branchName, astTree.toString(),codemessage,serviceName );

persistenceService.saveCodeResolution(errorHash,summary,serviceName,prUrl);

        return "GitHub Pull Request successfully created for class: " + targetClass;
    }
























    @Tool(description = "Use this tool ONLY for database connection drops, network timeouts, or infrastructure failures to trigger the SRE dashboard alert.")
    public String triggerInfrastructureDashboardAlert(@ToolParam(description = "ServiceName which  have a issue ")
                                                          String serviceName,
                                                      @ToolParam(description = "Issue Details what type of issue like db service not load ," +
                                                              " service discovering not fetch by any service etc. ")
                                                      String issueDetails, ToolContext context) {

log.info("Normal Service error correction ");
          String answer=chatClient.prompt()
                  .system("""
                          You are an elite Infrastructure SRE Specialist.
                           When an infrastructure error is passed to you,
                            analyze the root cause and output a strict, ultra-concise runbook using
                             EXACTLY 3 lines with short technical keywords:
                          
                          Line 1: [CAUSE]: [Why the failure occurred in 3-5 short keywords]
                          Line 2: [FIX 1]: [Immediate operational action/command in 3-5 short keywords]
                          Line 3: [FIX 2]: [Secondary verification or scaling action in 3-5 short keywords]
                          
                          CRITICAL CONSTRAINTS:
                          - Do not write paragraphs, explanations, or conversational filler.
                          - Use strict technical keyword
                          
                          
                          """)
                  .user("""
                          Service Name : %s
                          
                          Issue : %s
                          
                          """.formatted(serviceName,issueDetails)).call()
                  .content();

          String logs=(String) context.getContext().get("normalizedTrace");

        String hash = similarityFind.generateSha256(logs);

        resultService.saveTheSuccessResponse(hash,serviceName,logs,answer);

        return "Infrastructure runbook generated and pushed to developer dashboard for service: " + serviceName;
    }







}
