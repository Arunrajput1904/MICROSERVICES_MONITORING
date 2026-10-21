package com.aiopsengineservice.Repository;

import com.aiopsengineservice.Entity.DashboardDeveloperTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.nio.file.LinkOption;
import java.util.Optional;

public interface DashboardDeveloperTriggerRepo extends JpaRepository<DashboardDeveloperTrigger, Long> {

    @Query("select e from   DashboardDeveloperTrigger e where e.errorHash=:errorHash AND e.status=:Status")
    Optional<DashboardDeveloperTrigger> findByErrorHashAndSTATUS(@Param("errorHash") String errorHash,
                                                                 @Param("Status") String active);

    @Query("select e from   DashboardDeveloperTrigger e where e.errorHash=:errorHash")
    Optional<DashboardDeveloperTrigger> findByErrorHash(@Param("errorHash") String errorHash);
}
