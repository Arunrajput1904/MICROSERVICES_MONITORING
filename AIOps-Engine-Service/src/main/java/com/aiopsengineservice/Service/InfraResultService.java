package com.aiopsengineservice.Service;

import com.aiopsengineservice.Entity.DashboardDeveloperTrigger;
import com.aiopsengineservice.Entity.ResolvedIncident;
import com.aiopsengineservice.Repository.DashboardDeveloperTriggerRepo;
import com.aiopsengineservice.Repository.ResolvedIncidentRepo;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InfraResultService {

    private final DashboardDeveloperTriggerRepo developerTrigger;
    private final VectorStore vectorStore;
    private final ResolvedIncidentRepo resolvedIncidentRepo;

    public InfraResultService(DashboardDeveloperTriggerRepo developerTrigger, VectorStore vectorStore, ResolvedIncidentRepo resolvedIncidentRepo) {
        this.developerTrigger = developerTrigger;

        this.vectorStore = vectorStore;
        this.resolvedIncidentRepo = resolvedIncidentRepo;
    }



@Transactional
    void saveTheSuccessResponse(String errorHash,
                                String serviceName,
                                String sanitizedLog,
                                String runbookContent){


    Optional<DashboardDeveloperTrigger> active = developerTrigger.findByErrorHashAndSTATUS(errorHash, "ACTIVE");

     if(active.isPresent()){
         DashboardDeveloperTrigger dashboardDeveloperTrigger = active.get();
         Integer occurrenceCount = dashboardDeveloperTrigger.getOccurrenceCount();
          dashboardDeveloperTrigger.setOccurrenceCount(occurrenceCount+1);
         developerTrigger.save(dashboardDeveloperTrigger);


         Optional<ResolvedIncident> byHashError = resolvedIncidentRepo.findByHashError(errorHash);


         if(byHashError.isPresent()){
             ResolvedIncident resolvedIncident = byHashError.get();
             resolvedIncident.setOccurrenceCount(resolvedIncident.getOccurrenceCount()+1);
             resolvedIncidentRepo.save(resolvedIncident);
         }

     }
     else{
         DashboardDeveloperTrigger newTrigger = new DashboardDeveloperTrigger();
         newTrigger.setErrorHash(errorHash);
         newTrigger.setServiceName(serviceName);
         newTrigger.setRunbookContent(runbookContent);
         newTrigger.setStatus("ACTIVE");
         newTrigger.setOccurrenceCount(1);
         developerTrigger.save(newTrigger);

         ResolvedIncident newIncident = new ResolvedIncident();
         newIncident.setErrorHash(errorHash);
         newIncident.setCategory("INFRA");
         newIncident.setServiceName(serviceName);
         newIncident.setResolutionData(runbookContent);
         newIncident.setResolutionStatus("RUNBOOK_ACTIVE");
         newIncident.setOccurrenceCount(1);
         resolvedIncidentRepo.save(newIncident);

         Map<String, Object> metadata = Map.of(
                 "errorHash", errorHash,
                 "category", "INFRA"
         );
         Document vectorDoc=new Document(sanitizedLog,metadata);

         vectorStore.add(List.of(vectorDoc));
     }
}
}
