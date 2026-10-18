package com.aiopsengineservice.Repository;

import com.aiopsengineservice.Entity.IncidentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentRecordRepo extends JpaRepository<IncidentRecord,Long> {
    Optional<IncidentRecord> findByHash(String errorHash);
}
