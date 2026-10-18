package com.aiopsengineservice.Service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
@Service
public class LogSentizeService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");

    private static final Pattern IP_PATTERN =
            Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");

    private static final Pattern BEARER_TOKEN_PATTERN =
            Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\?[A-Za-z0-9-_=]*");

    public String sanitize(String rawLog) {
        if (rawLog == null || rawLog.isBlank()) {
            return rawLog;
        }
        return rawLog.replaceAll("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", "***MASKED_EMAIL***")
                .replaceAll("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b", "***MASKED_IP***");

    }
}
