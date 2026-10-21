package com.aiopsengineservice.Service;

import com.aiopsengineservice.Entity.ResolvedIncident;
import com.aiopsengineservice.Repository.ResolvedIncidentRepo;
import jakarta.transaction.Transactional;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
public class PersistenceService {

    private final VectorStore vectorStore;
    private final ResolvedIncidentRepo resolvedIncidentRepo;

    public PersistenceService(VectorStore vectorStore, ResolvedIncidentRepo resolvedIncidentRepo) {
        this.vectorStore = vectorStore;
        this.resolvedIncidentRepo = resolvedIncidentRepo;
    }

    @Transactional
    public void saveCodeResolution(String errorHash, String summary, String serviceName, String prUrl) {

ResolvedIncident incident=new ResolvedIncident();

        incident.setErrorHash(errorHash);
        incident.setServiceName(serviceName);
        incident.setCategory("CODE_BUG");
        incident.setResolutionStatus("PR_CREATED");
        incident.setResolutionData(prUrl);
        resolvedIncidentRepo.save(incident);

        Document vectorDocument = new Document(summary, Map.of(
                "errorHash", errorHash,
                "serviceName", serviceName,
                "prUrl", prUrl
        ));

        vectorStore.add(List.of(vectorDocument));
    }
}
