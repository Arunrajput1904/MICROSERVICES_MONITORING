package com.aiopsengineservice.Service;
import com.aiopsengineservice.Entity.DashboardDeveloperTrigger;
import com.aiopsengineservice.Entity.IncidentRecord;
import com.aiopsengineservice.Entity.ResolvedIncident;
import com.aiopsengineservice.Repository.DashboardDeveloperTriggerRepo;
import com.aiopsengineservice.Repository.IncidentRecordRepo;
import com.aiopsengineservice.Repository.ResolvedIncidentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service

public class SimilarityFind {

    private final DashboardDeveloperTriggerRepo developerTriggerRepo;
    private final VectorStore vectorStore;
    private final ResolvedIncidentRepo incidentRecordRepo;
    private final GeminiClient geminiClient;


    private final String owner = "Arunrajput1904";
    private final String repo = "MICROSERVICES_MONITORING";
    private final RestClient client;

    public SimilarityFind(DashboardDeveloperTriggerRepo developerTriggerRepo, VectorStore vectorStore,
                          ResolvedIncidentRepo incidentRecordRepo, @Lazy GeminiClient geminiClient,
                          RestClient.Builder client , @Value("${pat}") String token) {
        this.developerTriggerRepo = developerTriggerRepo;
        this.vectorStore = vectorStore;
        this.incidentRecordRepo = incidentRecordRepo;
        this.geminiClient = geminiClient;
        this.client = client
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization","Bearer "+token)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }
    public void messageinvoke(String sanitizedLog) {
        String normalizedTrace = extractStackTrace(sanitizedLog);
        String errorHash = generateSha256(normalizedTrace);


        Optional<ResolvedIncident> byHash = incidentRecordRepo.findByHashError(errorHash);

        if(byHash.isPresent() && byHash.get().getLastSeenAt().isAfter(LocalDateTime.now().minusMinutes(10))){
            return;
        }

//log.info(byHash.isPresent()+"........................");
        if(byHash.isPresent()){

            ResolvedIncident incident = byHash.get();

            boolean result =funcexecution(incident);
            if (result){
                return;
            }
        }

        List<Document> l2Results =vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(normalizedTrace)
                        .topK(1)
                        .build()
        );


        if(!l2Results.isEmpty()){
            Document bestmatch=l2Results.getFirst();

        Float similarityScore= (Float) bestmatch.getMetadata().get("distance");
        similarityScore=1-similarityScore;

            if(similarityScore>=0.90){

                log.info("Similarity findout");
               String errorHashs=(String) bestmatch.getMetadata().get("errorHash");

                Optional<ResolvedIncident> byHashs = incidentRecordRepo.findByHashError(errorHashs);



                if(byHashs.isPresent()){

                    ResolvedIncident incident = byHashs.get();

                    boolean result =funcexecution(incident);
                    if (result){
                        return;
                    }
                }
            }
        }

log.info("Gen ca;;;;;eeeeeeeeeeed");
        geminiClient.analyzeError(normalizedTrace);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
     boolean funcexecution(ResolvedIncident incident) {
        if(incident.getCategory().equals("INFRA")){


            Optional<DashboardDeveloperTrigger> byErrorHash = developerTriggerRepo.findByErrorHash(incident.getErrorHash());

            DashboardDeveloperTrigger dashboardDeveloperTrigger = byErrorHash.get();

            dashboardDeveloperTrigger.setOccurrenceCount(dashboardDeveloperTrigger.getOccurrenceCount()+1);
            dashboardDeveloperTrigger.setStatus("ACTIVE");

            developerTriggerRepo.save(dashboardDeveloperTrigger);

            return true;

        }
        else{


            String prUrl=incident.getResolutionData();
            try {
                String[] segments = prUrl.split("/");
                String pullNumberStr = segments[segments.length - 1];
                int pullNumber = Integer.parseInt(pullNumberStr);


                Map<String, Object> prResponse = client.get()
                        .uri("/repos/{owner}/{repo}/pulls/{pull_number}", owner, repo, pullNumber)
                        .retrieve()
                        .body(Map.class);

                if (prResponse != null && prResponse.containsKey("merged")) {
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Failed to check PR merge status: " + e.getMessage());
            }

            return false;

        }
    }


    private String extractStackTrace(String log) {
        return log.replaceAll("^\\d{4}-\\d{2}-\\d{2}.*?(java\\.|com\\.)", "$1");
    }

    String generateSha256(String input) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(input);
    }

}
