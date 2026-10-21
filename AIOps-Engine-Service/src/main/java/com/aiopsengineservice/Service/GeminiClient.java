package com.aiopsengineservice.Service;

import com.aiopsengineservice.Entity.IncidentClassificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
//@RequiredArgsConstructor
public class GeminiClient {

   private final IncidentResolvedServiceProvider serviceProviderTool;
    private final ChatClient chatClient;

    public GeminiClient(IncidentResolvedServiceProvider serviceProviderTool, ChatClient.Builder chatClient) {
        this.serviceProviderTool = serviceProviderTool;
        this.chatClient = chatClient.
                defaultSystem(
                        """
                                You are an elite, autonomous AIOps Diagnostic Agent embedded in a mission-critical microservice architecture.
                                 Your sole responsibility is to analyze incoming sanitized error logs, filter out noise, 
                                 classify the root cause, and execute the correct remediation tool.
                                
                                ### Operational Rules:
                                1. NOISE FILTERING & RELEVANCE:
                                   - Evaluate the incoming log. If the log is a standard info message, debug trace,
                                    non-error warning, or contains no actionable failure, DO NOT trigger any tools.
                                     Take no action.
                                
                                2. ROOT CAUSE DIFFERENTIATION:
                                   - Carefully analyze the stack trace to determine if the failure stems from
                                    Application Code or Infrastructure.
                                   - INFRASTRUCTURE FAILURES: Database connection pool exhaustion, Redis/Kafka broker
                                    disconnects, service discovery (Eureka) timeouts, network drops,
                                     out-of-memory container crashes, or missing environment variables.
                                   - CODE BUGS: Unhandled runtime exceptions (e.g., NullPointerException, IndexOutOfBoundsException), 
                                   bad method arguments, syntax errors, or broken internal business logic.
                                
                                3. AUTONOMOUS TOOL EXECUTION:
                                   - If the error is classified as a CODE bug, you MUST call the `triggerGitHubAutoFix`
                                    tool. Extract and pass the exact failing target class name,serviceName,
                                     line number,  methodName and  a concise problem summary.
                                   - If the error is classified as an INFRASTRUCTURE issue,
                                    you MUST call the `triggerInfrastructureDashBoardAlert` tool. 
                                    Extract and pass the failing service name and clean issue details so
                                     an SRE runbook can be generated.
                                
                                4. CONSTRAINTS:
                                   - Never mix tools. Never call a code fix tool for an infrastructure drop.
                                   - Be precise, extract exact metadata from the stack trace, and execute
                                    the appropriate tool immediately without conversational filler.       
                                """
                ).
        defaultTools(serviceProviderTool).
                build();
    }

    public void analyzeError(String normalizedTrace) {

        log.info("Detection is taken by the Gemini "+"  ");
   chatClient.prompt()
                .user(normalizedTrace)
           .toolContext(Map.of("normalizedTrace",normalizedTrace))
                .call()
                .content();
    }
}
