package com.aiopsengineservice.Repository;

import com.aiopsengineservice.Entity.IncidentRecord;
import com.aiopsengineservice.Entity.ResolvedIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResolvedIncidentRepo  extends JpaRepository<ResolvedIncident,Long> {

    @Query("select e from ResolvedIncident e where e.errorHash=:errorHash")
    Optional<ResolvedIncident> findByHashError(@Param("errorHash") String errorHash);


}
