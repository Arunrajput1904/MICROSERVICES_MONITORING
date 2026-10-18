package com.aiopsengineservice.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {


    private final LogSentizeService logSentizeService;
   private final SimilarityFind similarityFind;

    @KafkaListener(topics = "live-app-logs", groupId = "aiops-logs-group-v2")
    public void generatelogs(String message){
        log.debug("logs are fetched Successfully :");


        log.info("Logs sentizing");
        String sanitizedLog = logSentizeService.sanitize(message);



        log.info("Data sentized Successfully"+" ->>>>>>>>>>"+sanitizedLog);

        if (sanitizedLog.contains("ERROR") || sanitizedLog.contains("Exception")) {
                 similarityFind.messageinvoke(sanitizedLog);
        }
    }



}
