package com.aiopsengineservice.Schedular;


import com.aiopsengineservice.Entity.MetricSnapshot;
import com.aiopsengineservice.Entity.ServiceCircuitState;
import com.aiopsengineservice.Repository.MetricSnapshotRepo;
import com.aiopsengineservice.Repository.ServiceCircuitStateRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service

@RequiredArgsConstructor
public class ServiceDiscoveryScheduler {

private final RestTemplate restTemplate;
private final MetricSnapshotRepo metricSnapshotRepo;
    private final DiscoveryClient discoveryClient;

    private final ServiceCircuitStateRepository circuitStateRepository;

    private static final double CPU_THRESHOLD = 0.85;
    private static final Set<String> IGNORED_SERVICES = Set.of(

            "servicediscovery".toUpperCase(),
            "CONFIG-SERVER",
            "AIOPS-ENGINE-SERVICE",
            "API-GATEWAY"
    );
    @PostConstruct
    public void init() {
        log.info(">>> ServiceDiscoveryScheduler Loaded <<<");
    }

    @Scheduled(fixedRate = 15000, initialDelay = 5000)
    void schedulingDepth(){

        log.info("services");
        List<String>servicesName=discoveryClient.getServices();

        List<String> serviceList = servicesName.stream()
                .filter(x -> x != null && !IGNORED_SERVICES.contains(x.toUpperCase()))
                .toList();


        log.info("services"+ serviceList);


        serviceList.stream()
                .forEach(this::evaluateService);

    }

    private void evaluateService(String serviceName) {

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        if (instances.isEmpty()) {
            log.warn("No active instances returned from Eureka for service: {}", serviceName);
            return;
        }

        log.info("Polling metrics for service: {} across {} active instance(s)", serviceName, instances.size());


        for (ServiceInstance instance : instances) {
            fetchAndSaveInstanceMetrics(serviceName, instance);
        }



    }

    private void fetchAndSaveInstanceMetrics(String serviceName, ServiceInstance instance) {

        String baseUrl = instance.getUri().toString();
        String  instanceId=(instance.getInstanceId()!=null) ? instance.getInstanceId() : baseUrl ;



        try{
            String cpuUrl = baseUrl + "/actuator/metrics/process.cpu.usage";

            JsonNode cpuResponse=restTemplate.getForObject(cpuUrl,JsonNode.class);

            double cpuUsage=extractMetricValue(cpuResponse);

            String memUrl = baseUrl + "/actuator/metrics/jvm.memory.used";
            JsonNode memResponse = restTemplate.getForObject(memUrl, JsonNode.class);
            double memoryUsedBytes = extractMetricValue(memResponse);

            String reqUrl = baseUrl + "/actuator/metrics/http.server.requests.active";
            JsonNode reqResponse = restTemplate.getForObject(reqUrl, JsonNode.class);
            double reqUsedBytes = extractMetricValue(reqResponse);


            String dbActiveUrl = baseUrl + "/actuator/metrics/hikaricp.connections.active";
            JsonNode dbActiveResponse = restTemplate.getForObject(dbActiveUrl, JsonNode.class);
            double dbActiveUsedBytes = extractMetricValue(dbActiveResponse);


            String dbPendingUrl = baseUrl + "/actuator/metrics/hikaricp.connections.pending";
            JsonNode dbPendingResponse = restTemplate.getForObject(dbPendingUrl, JsonNode.class);
            double dbPendingUrlUsedBytes = extractMetricValue(dbPendingResponse);


            String threadsUrl = baseUrl + "/actuator/metrics/jvm.threads.live";
            JsonNode threadsResponse = restTemplate.getForObject(threadsUrl, JsonNode.class);
            double threadsResponseUsedBytes = extractMetricValue(threadsResponse);


            String errorsUrl = baseUrl + "/actuator/metrics/logback.events?tag=level:error";
            JsonNode errorsResponse = restTemplate.getForObject(errorsUrl, JsonNode.class);
            double errorsUsedBytes = extractMetricValue(errorsResponse);


            MetricSnapshot snapshot = MetricSnapshot.builder()
                    .capturedAt(LocalDateTime.now())
                    .serviceName(serviceName)
                    .instanceId(instanceId)
                    .cpuUsage(cpuUsage)
                    .memoryUsedMb(memoryUsedBytes / (1024 * 1024))
                    .activeRequests(reqUsedBytes)
                    .dbActiveConnections(dbActiveUsedBytes)
                    .dbPendingConnections(dbPendingUrlUsedBytes)
                    .liveThreads(threadsResponseUsedBytes)
                    .errorLogsCount(errorsUsedBytes)
                    .build();

            metricSnapshotRepo.save(snapshot);

log.info("DATA SUCCESSFULLY STORED .................");
            evaluateServiceHealth(serviceName);
        }
        catch (Exception e){
          log.error(e.getMessage()+"  "+"404 status   ");
        }


    }

    @Transactional
    public void evaluateServiceHealth(String serviceName) {

        List<MetricSnapshot> recentSnapshots = metricSnapshotRepo.findTop3ByServiceNameOrderByCapturedAtDesc(serviceName);


        if (recentSnapshots.size() < 3) {
            log.debug("Insufficient snapshots for evaluation on service: {}. Found: {}", serviceName, recentSnapshots.size());
            return;
        }


        List<MetricSnapshot> list = recentSnapshots.stream()
                .filter(x -> x.getCpuUsage() >= CPU_THRESHOLD)
                .toList();


        boolean allOverloaded = list.size() == 3;

        ServiceCircuitState circuitState = circuitStateRepository.findByServiceName(serviceName)
                .orElseGet(() -> ServiceCircuitState.builder()
                        .serviceName(serviceName)
                        .status("NORMAL")
                        .updatedAt(LocalDateTime.now())
                        .build());


        if (allOverloaded && !"SHIELDED".equalsIgnoreCase(circuitState.getStatus())) {
            log.warn("CRITICAL: Service {} sustained high load for 3 consecutive polls (45s). Activating SHIELDED state!", serviceName);
            circuitState.setStatus("SHIELDED");
            circuitState.setUpdatedAt(LocalDateTime.now());
            circuitStateRepository.save(circuitState);

        } else if (!allOverloaded && "SHIELDED".equalsIgnoreCase(circuitState.getStatus())) {
            log.info("RECOVERY: Service {} metrics normalized. Resetting state to NORMAL.", serviceName);
            circuitState.setStatus("NORMAL");
            circuitState.setUpdatedAt(LocalDateTime.now());
            circuitStateRepository.save(circuitState);
        }
    }

    private double extractMetricValue(JsonNode response) {

        if (response != null && response.has("measurements")) {
            return response.get("measurements").get(0).get("value").asDouble();
        }
        return 0.0;
    }





}