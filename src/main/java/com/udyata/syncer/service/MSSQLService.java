package com.udyata.syncer.service;

import com.udyata.syncer.mssql.entity.MSSQLDeviceLogs;
import com.udyata.syncer.mssql.repository.MSSQLDeviceLogsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MSSQLService {
    @PersistenceContext(unitName = "mssqlEntityManager")
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<MSSQLDeviceLogs> getUnprocessedLogs() {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime startOfDay = currentDate.toLocalDate().atStartOfDay();

        String tableName = String.format("DeviceLogs_%d_%d",
                currentDate.getMonthValue(),
                currentDate.getYear());

        String sql = String.format("""
            SELECT TOP 1000 dl.* 
            FROM [dbo].[%s] dl WITH (NOLOCK)
            WHERE dl.LogDate >= :startOfDay
            ORDER BY dl.LogDate
            """, tableName);

        try {
            return entityManager.createNativeQuery(sql, MSSQLDeviceLogs.class)
                    .setParameter("startOfDay", startOfDay)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error executing SQL: {}", e.getMessage());
            throw e;
        }
    }
}
