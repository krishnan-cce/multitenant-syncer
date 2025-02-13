package com.udyata.syncer.service;

import com.udyata.syncer.mssql.entity.MSSQLDeviceLogs;
import com.udyata.syncer.mssql.repository.MSSQLDeviceLogsRepository;
import com.udyata.syncer.mysql.entity.MySQLPunchingDetails;
import com.udyata.syncer.mysql.repository.MySQLPunchingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class DataSyncService {
    private final MSSQLService mssqlService;
    private final MySQLPunchingRepository mysqlRepository;

    @Autowired
    @Qualifier("mssqlTransactionTemplate")
    private TransactionTemplate mssqlTransactionTemplate;

    @Autowired
    @Qualifier("mysqlTransactionTemplate")
    private TransactionTemplate mysqlTransactionTemplate;

    @Scheduled(fixedRateString = "${sync.interval.milliseconds}")
    public void syncData() {
        try {
            // Read from MSSQL using MSSQL transaction with pagination
            List<MSSQLDeviceLogs> unprocessedLogs = mssqlTransactionTemplate.execute(status -> {
                return mssqlService.getUnprocessedLogs();
            });

            if (unprocessedLogs != null && !unprocessedLogs.isEmpty()) {
                // Process in chunks to avoid memory issues
                int chunkSize = 1000;
                for (int i = 0; i < unprocessedLogs.size(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, unprocessedLogs.size());
                    List<MSSQLDeviceLogs> chunk = unprocessedLogs.subList(i, end);

                    // Write chunk to MySQL using MySQL transaction
                    mysqlTransactionTemplate.execute(status -> {
                        processUnprocessedLogs(chunk);
                        return null;
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error during sync: ", e);
            throw e;
        }
    }

    protected void processUnprocessedLogs(List<MSSQLDeviceLogs> unprocessedLogs) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<MySQLPunchingDetails> batchToInsert = new ArrayList<>();
        int batchSize = 50;
        int totalProcessed = 0;

        for (MSSQLDeviceLogs mssqlLog : unprocessedLogs) {
            String deviceLogId = String.valueOf(mssqlLog.getDeviceLogId());
            if (!mysqlRepository.existsByDeviceName(deviceLogId)) {
                MySQLPunchingDetails punchingDetails = MySQLPunchingDetails.builder()
                        .empCode(mssqlLog.getUserId())
                        .punchingDate(mssqlLog.getLogDate())
                        .punchingMode(mssqlLog.getDirection())
                        .entryDate(currentTime)
                        .deviceName(deviceLogId)
                        .deviceLogId(mssqlLog.getDeviceLogId())
                        .locationName(mssqlLog.getLocationAddress())
                        .processedFlag("N")
                        .build();

                batchToInsert.add(punchingDetails);

                if (batchToInsert.size() >= batchSize) {
                    mysqlRepository.saveAll(batchToInsert);
                    totalProcessed += batchToInsert.size();
                    batchToInsert.clear();
                }
            }
        }

        if (!batchToInsert.isEmpty()) {
            mysqlRepository.saveAll(batchToInsert);
            totalProcessed += batchToInsert.size();
        }

        log.info("Successfully synced {} records", totalProcessed);
    }
}

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DataSyncService {
//    private final MSSQLService mssqlService;
//    private final MySQLPunchingRepository mysqlRepository;
//
//    @Autowired
//    @Qualifier("mssqlTransactionTemplate")
//    private TransactionTemplate mssqlTransactionTemplate;
//
//    @Autowired
//    @Qualifier("mysqlTransactionTemplate")
//    private TransactionTemplate mysqlTransactionTemplate;
//
//    @Scheduled(fixedRateString = "${sync.interval.milliseconds}")
//    public void syncData() {
//        try {
//            // Read from MSSQL using MSSQL transaction
//            List<MSSQLDeviceLogs> unprocessedLogs = mssqlTransactionTemplate.execute(status -> {
//                return mssqlService.getUnprocessedLogs();
//            });
//
//            if (unprocessedLogs != null && !unprocessedLogs.isEmpty()) {
//                // Write to MySQL using MySQL transaction
//                mysqlTransactionTemplate.execute(status -> {
//                    processUnprocessedLogs(unprocessedLogs);
//                    return null;
//                });
//            }
//        } catch (Exception e) {
//            log.error("Error during sync: ", e);
//            throw e;
//        }
//    }
//
//    protected void processUnprocessedLogs(List<MSSQLDeviceLogs> unprocessedLogs) {
//        LocalDateTime currentTime = LocalDateTime.now();
//        List<MySQLPunchingDetails> batchToInsert = new ArrayList<>();
//        int batchSize = 50;
//        int totalProcessed = 0;
//
//        for (MSSQLDeviceLogs mssqlLog : unprocessedLogs) {
//            String deviceLogId = String.valueOf(mssqlLog.getDeviceLogId());
//            // Check if record already exists before adding to batch
//            if (!mysqlRepository.existsByDeviceName(deviceLogId)) {
//                MySQLPunchingDetails punchingDetails = MySQLPunchingDetails.builder()
//                        .empCode(mssqlLog.getUserId())
//                        .punchingDate(mssqlLog.getLogDate())
//                        .punchingMode(mssqlLog.getDirection())
//                        .entryDate(currentTime)
//                        .deviceName(deviceLogId)
//                        .deviceLogId(mssqlLog.getDeviceLogId())
//                        .locationName(mssqlLog.getLocationAddress())
//                        .processedFlag("N")
//                        .build();
//
//                batchToInsert.add(punchingDetails);
//
//                if (batchToInsert.size() >= batchSize) {
//                    mysqlRepository.saveAll(batchToInsert);
//                    totalProcessed += batchToInsert.size();
//                    batchToInsert.clear();
//                }
//            }
//        }
//
//        if (!batchToInsert.isEmpty()) {
//            mysqlRepository.saveAll(batchToInsert);
//            totalProcessed += batchToInsert.size();
//        }
//
//        log.info("Successfully synced {} records", totalProcessed);
//    }
//}