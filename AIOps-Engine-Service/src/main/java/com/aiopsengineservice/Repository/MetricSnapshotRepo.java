package com.aiopsengineservice.Repository;

import com.aiopsengineservice.Entity.MetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetricSnapshotRepo extends JpaRepository<MetricSnapshot,Long> {
    List<MetricSnapshot> findTop3ByServiceNameOrderByCapturedAtDesc(String serviceName);
}
