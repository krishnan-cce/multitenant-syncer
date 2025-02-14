package com.udyata.syncer.mysql.repository;


import com.udyata.syncer.mysql.entity.MySQLPunchingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;

public interface MySQLPunchingRepository extends JpaRepository<MySQLPunchingDetails, Long> {
    @Query("SELECT p.deviceLogId FROM MySQLPunchingDetails p WHERE p.punchingDate >= :startDate")
    Set<Integer> findProcessedDeviceLogIds(@Param("startDate") LocalDateTime startDate);

    boolean existsByDeviceLogId(Integer deviceLogId);
}