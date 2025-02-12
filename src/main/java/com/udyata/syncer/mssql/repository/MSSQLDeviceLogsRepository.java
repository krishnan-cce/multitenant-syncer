package com.udyata.syncer.mssql.repository;

import com.udyata.syncer.mssql.entity.MSSQLDeviceLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MSSQLDeviceLogsRepository extends JpaRepository<MSSQLDeviceLogs, Integer> {
    @Query(value = """
        SELECT dl.* FROM [dbo].[DeviceLogs_:month_:year] dl 
        WHERE NOT EXISTS (
            SELECT 1 FROM hr_punching_dtls hpd 
            WHERE CAST(hpd.device_name AS VARCHAR) = CAST(dl.DeviceLogId AS VARCHAR)
        ) 
        AND dl.LogDate >= :startDate
        """, nativeQuery = true)
    List<MSSQLDeviceLogs> findUnprocessedLogs(
            @Param("month") int month,
            @Param("year") int year,
            @Param("startDate") LocalDateTime startDate
    );
}