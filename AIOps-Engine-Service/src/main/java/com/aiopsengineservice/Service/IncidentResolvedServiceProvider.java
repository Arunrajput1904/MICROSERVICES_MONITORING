package com.aiopsengineservice.Service;


import com.aiopsengineservice.Entity.CodePatchResponse;
import com.aiopsengineservice.Entity.IncidentClassificationDto;
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
        String rawMethodCode = githubApiService.fetchMethodCode(serviceName, targetClass, methodName, lineNumber);

        String sanitizedLog = (String) context.getContext().get("normalizedTrace");
        String coderPrompt = String.format("""
                You are a strict Java code reviewer.
                
                             Original Error:
                             %s
                
                             Original Code:
                             %s
                
                             Proposed Patch:
                             %s
                
                             Review the patch for correctness, syntax, and consistency
                             with the original error.
                
                             Return all three fields:
                             - rootCauseExplanation: the actual root cause
                             - fixedMethodCode: the complete corrected Java method
                             - commitMessage: a concise commit message
                
                             Populate every field with a meaningful, non-null value.
                             Do not invent missing source code.
                """, sanitizedLog, targetClass, methodName, lineNumber, rawMethodCode);
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

YOUR GOAL:
Analyze the provided Original Error, Original Code, and Proposed Patch. Review the patch for syntax, correctness, and consistency. If the proposed patch is invalid, hallucinated, or incomplete, discard it and generate a complete, syntactically valid Java method replacement.

INPUT CONTEXT:
Original Error:
%s

Original Code:
%s

Proposed Patch:
%s

STRICT GROUNDING RULES:
1. Adhere Strictly to Original Code: Do NOT invent non-existent variables, classes, parameters, or placeholder comments (such as `// existing logic`, `data`, or `TODO`).
2. Method Signature Integrity: Do NOT modify the original method signature (annotations, return type, method name, or parameters) unless the error stack trace explicitly demands a signature change.
3. Complete Implementation: The `fixedMethodCode` MUST contain a fully implemented, compilable Java method—not a snippet or partial block.
4. Error-Line Alignment: Map the stack trace frame to the exact line in "Original Code". If the method explicitly throws an artificial test error (e.g., `throw new NullPointerException(...)`), replace it with a valid domain response (e.g., returning `ResponseEntity.ok(...)`).

OUTPUT REQUIREMENTS:
You must populate ALL three fields with non-null values:
- rootCauseExplanation: A clear explanation of why the original code failed based on the stack trace.
- fixedMethodCode: The complete, corrected, and compilable Java method.
- commitMessage: A concise conventional commit message (e.g., "fix(controller): resolve NullPointerException in getdata endpoint").

OUTPUT FORMATTING CONSTRAINTS:
- Output MUST be valid, unformatted JSON matching the required schema.
- Do NOT wrap the JSON in markdown code blocks (do NOT use ```json or ```).
- Do NOT include any conversational text outside the raw JSON object.
""", sanitizedLog, rawMethodCode, result.getFixedMethodCode());


        CodePatchResponse validatedPatch = chatClient.prompt()
                .user(validatorPrompt)
                .call()
                .entity(CodePatchResponse.class);

        log.info("File validation end ............................................."+validatedPatch);

        String codepush=validatedPatch.getFixedMethodCode();
        String codemessage=validatedPatch.getCommitMessage();

        String errorHash= similarityFind.generateSha256(sanitizedLog);
        String branchName = "aiops-fix-" + UUID.randomUUID().toString().substring(0, 8);

        String prUrl = githubApiService.createPullRequestWithPatch(targetClass, branchName, codepush,codemessage,serviceName );

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
