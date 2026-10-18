package com.aiopsengineservice.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    @Column(name = "cpu_usage_pct")
    private Double cpuUsage;

    @Column(name = "memory_used_mb")
    private Double memoryUsedMb;

    @Column(name = "active_http_requests")
    private Double activeRequests;

    @Column(name = "db_active_connections")
    private Double dbActiveConnections;

    @Column(name = "db_pending_connections")
    private Double dbPendingConnections;

    @Column(name = "live_threads")
    private Double liveThreads;

    @Column(name = "error_logs_count")
    private Double errorLogsCount;
}