package com.udyata.syncer.service;

import com.udyata.syncer.mssql.entity.MSSQLDeviceLogs;
import com.udyata.syncer.mssql.repository.MSSQLDeviceLogsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        // First, get total count
        String countSql = String.format("""
            SELECT COUNT(*) 
            FROM [dbo].[%s] dl WITH (NOLOCK)
            WHERE dl.LogDate >= :startOfDay
            """, tableName);

        // Fix: Handle count result as Long
        Number count = (Number) entityManager.createNativeQuery(countSql)
                .setParameter("startOfDay", startOfDay)
                .getSingleResult();

        long totalCount = count.longValue();
        log.info("Total records to process: {}", totalCount);

        List<MSSQLDeviceLogs> allLogs = new ArrayList<>();
        int pageSize = 1000;
        int offset = 0;

        // SQL Server pagination query
        String sql = String.format("""
            SELECT dl.* 
            FROM [dbo].[%s] dl WITH (NOLOCK)
            WHERE dl.LogDate >= :startOfDay
            ORDER BY dl.LogDate OFFSET :offset ROWS 
            FETCH NEXT :pageSize ROWS ONLY
            """, tableName);

        while (true) {
            try {
                List<MSSQLDeviceLogs> pagedResults = entityManager.createNativeQuery(sql, MSSQLDeviceLogs.class)
                        .setParameter("startOfDay", startOfDay)
                        .setParameter("offset", offset)
                        .setParameter("pageSize", pageSize)
                        .getResultList();

                if (pagedResults.isEmpty()) {
                    break;
                }

                allLogs.addAll(pagedResults);
                offset += pageSize;

                log.debug("Fetched {} records, total so far: {}", pagedResults.size(), allLogs.size());

                if (pagedResults.size() < pageSize) {
                    break;
                }
            } catch (Exception e) {
                log.error("Error executing SQL at offset {}: {}", offset, e.getMessage());
                throw e;
            }
        }

        log.info("Successfully fetched all {} records", allLogs.size());
        return allLogs;
    }
}

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class MSSQLService {
//    @PersistenceContext(unitName = "mssqlEntityManager")
//    private EntityManager entityManager;
//
//    @Transactional(readOnly = true)
//    public List<MSSQLDeviceLogs> getUnprocessedLogs() {
//        LocalDateTime currentDate = LocalDateTime.now();
//        LocalDateTime startOfDay = currentDate.toLocalDate().atStartOfDay();
//
//        String tableName = String.format("DeviceLogs_%d_%d",
//                currentDate.getMonthValue(),
//                currentDate.getYear());
//
//        String sql = String.format("""
//            SELECT TOP 1000 dl.*
//            FROM [dbo].[%s] dl WITH (NOLOCK)
//            WHERE dl.LogDate >= :startOfDay
//            ORDER BY dl.LogDate
//            """, tableName);
//
//        try {
//            return entityManager.createNativeQuery(sql, MSSQLDeviceLogs.class)
//                    .setParameter("startOfDay", startOfDay)
//                    .getResultList();
//        } catch (Exception e) {
//            log.error("Error executing SQL: {}", e.getMessage());
//            throw e;
//        }
//    }
//}
