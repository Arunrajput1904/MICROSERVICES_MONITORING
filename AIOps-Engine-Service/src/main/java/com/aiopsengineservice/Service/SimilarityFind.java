package com.aiopsengineservice.Service;
import com.aiopsengineservice.Entity.IncidentRecord;
import com.aiopsengineservice.Repository.IncidentRecordRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SimilarityFind {

    private final VectorStore vectorStore;
    private final IncidentRecordRepo incidentRecordRepo;
    private final GeminiClient geminiClient;
    public String messageinvoke(String sanitizedLog) {
        String normalizedTrace = extractStackTrace(sanitizedLog);
        String errorHash = generateSha256(normalizedTrace);


        Optional<IncidentRecord> byHash = incidentRecordRepo.findByHash(errorHash);



        if(byHash.isPresent()){
            return byHash.get().getResolution();
        }

        List<Document> l2Results =vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(normalizedTrace)
                        .topK(1)
                        .build()
        );


        if(!l2Results.isEmpty()){
            Document bestmatch=l2Results.getFirst();

            double similarityScore=(double) bestmatch.getMetadata().get("distance");

            if(similarityScore>=0.90){
                return (String) bestmatch.getMetadata().get("resolution_steps");
            }
        }
        String generatedResolution = geminiClient.analyzeError(normalizedTrace);
        populateCachesAsync(errorHash, normalizedTrace, generatedResolution);
        return generatedResolution;
    }
    @Async
    public void populateCachesAsync(String hash, String trace, String resolution) {
        incidentRecordRepo.save(new IncidentRecord(hash, trace, resolution));

        Document doc = new Document(trace, Map.of(
                "error_hash", hash,
                "resolution_steps", resolution
        ));
        vectorStore.add(List.of(doc));
    }

    private String extractStackTrace(String log) {
        return log.replaceAll("^\\d{4}-\\d{2}-\\d{2}.*?(java\\.|com\\.)", "$1");
    }

    private String generateSha256(String input) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(input);
    }

}
