package com.aiopsengineservice.Repository;

import com.aiopsengineservice.Entity.ServiceCircuitState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceCircuitStateRepository extends JpaRepository<ServiceCircuitState, Long> {
    Optional<ServiceCircuitState> findByServiceName(String serviceName);
}