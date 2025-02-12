package com.udyata.syncer.mysql.repository;


import com.udyata.syncer.mysql.entity.MySQLPunchingDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MySQLPunchingRepository extends JpaRepository<MySQLPunchingDetails, Long> {
    boolean existsByDeviceName(String deviceName);
}