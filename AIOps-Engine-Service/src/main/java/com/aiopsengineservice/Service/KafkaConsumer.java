package com.aiopsengineservice.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer  implements ConsumerSeekAware {


    private final LogSentizeService logSentizeService;
   private final SimilarityFind similarityFind;

    @KafkaListener(topics = "live-app-logs", groupId = "aiops-logs-group-v4")
    public void generatelogs(String message){
//try{
    log.debug("logs are fetched Successfully :");


        log.info("Logs sentizing");
        String sanitizedLog = logSentizeService.sanitize(message);
//
    if (sanitizedLog.contains("ERROR") || sanitizedLog.contains("Exception")) {
        similarityFind.messageinvoke(sanitizedLog);
    }
//}
//catch (Exception e){
//    log.info(e.getMessage()+" Error Occured ");
//}
    }


    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        callback.seekToEnd(assignments.keySet());
    }


}
