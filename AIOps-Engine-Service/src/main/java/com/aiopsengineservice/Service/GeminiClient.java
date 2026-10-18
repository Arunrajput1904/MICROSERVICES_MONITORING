package com.aiopsengineservice.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
//@RequiredArgsConstructor
public class GeminiClient {


    private final ChatClient chatClient;

    public GeminiClient(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    public String analyzeError(String normalizedTrace) {

        String res=chatClient.prompt(
                        """
                                You are an expert Java Site Reliability Engineer (SRE). Analyze the provided stack trace. Identify the root cause and provide 
                                                        3 clear, actionable steps to resolve the issue. Format the output in plain text
                                """
                )
                .user(normalizedTrace)
                .call().content();

        log.info(res);

        return res;
    }
}
