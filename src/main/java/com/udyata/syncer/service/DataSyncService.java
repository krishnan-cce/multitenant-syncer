package com.udyata.syncer.service;

import com.udyata.syncer.mssql.entity.MSSQLDeviceLogs;
import com.udyata.syncer.mssql.repository.MSSQLDeviceLogsRepository;
import com.udyata.syncer.mysql.entity.MySQLPunchingDetails;
import com.udyata.syncer.mysql.repository.MySQLPunchingRepository;
import com.udyata.syncer.mysqltemp.entity.MySQLTempPunchingDetails;
import com.udyata.syncer.mysqltemp.repository.MySQLTempPunchingRepository;
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
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class DataSyncService {
    private final MSSQLService mssqlService;
    private final MySQLPunchingRepository mysqlRepository;
    private final MySQLTempPunchingRepository mysqlTempRepository;

    @Scheduled(fixedRateString = "${sync.interval.milliseconds}")
    @Transactional
    public void syncData() {
        try {
            log.info("Starting data synchronization process");
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

            // Get processed IDs
            Set<Integer> processedIds = mysqlRepository.findProcessedDeviceLogIds(startOfDay);
            log.info("Found {} processed records for today", processedIds.size());

            // Get unprocessed logs
            List<MSSQLDeviceLogs> unprocessedLogs = mssqlService.getUnprocessedLogs();

            if (unprocessedLogs != null && !unprocessedLogs.isEmpty()) {
                processUnprocessedLogs(unprocessedLogs, processedIds);
            } else {
                log.info("No new logs to process");
            }
        } catch (Exception e) {
            log.error("Error during sync: ", e);
            throw e;
        }
    }

    @Transactional
    protected void processUnprocessedLogs(List<MSSQLDeviceLogs> unprocessedLogs, Set<Integer> processedIds) {
        log.info("Processing {} unprocessed logs", unprocessedLogs.size());
        LocalDateTime currentTime = LocalDateTime.now();
        List<MySQLPunchingDetails> batchToInsert = new ArrayList<>();
        List<MySQLTempPunchingDetails> tempBatchToInsert = new ArrayList<>();
        int batchSize = 50;

        for (MSSQLDeviceLogs mssqlLog : unprocessedLogs) {
            if (!processedIds.contains(mssqlLog.getDeviceLogId())) {
                batchToInsert.add(createPunchingDetails(mssqlLog, currentTime));
                tempBatchToInsert.add(createTempPunchingDetails(mssqlLog, currentTime));

                if (batchToInsert.size() >= batchSize) {
                    saveAndClearBatch(batchToInsert, tempBatchToInsert);
                }
            }
        }

        if (!batchToInsert.isEmpty()) {
            saveAndClearBatch(batchToInsert, tempBatchToInsert);
        }
    }

    private void saveAndClearBatch(List<MySQLPunchingDetails> batch, List<MySQLTempPunchingDetails> tempBatch) {
        mysqlRepository.saveAll(batch);
        mysqlTempRepository.saveAll(tempBatch);
        log.debug("Saved batch of {} records", batch.size());
        batch.clear();
        tempBatch.clear();
    }

    private MySQLPunchingDetails createPunchingDetails(MSSQLDeviceLogs log, LocalDateTime currentTime) {
        return MySQLPunchingDetails.builder()
                .empCode(log.getUserId())
                .punchingDate(log.getLogDate())
                .punchingMode(log.getDirection())
                .entryDate(currentTime)
                .deviceName(String.valueOf(log.getDeviceLogId()))
                .deviceLogId(log.getDeviceLogId())
                .locationName(log.getLocationAddress())
                .processedFlag("N")
                .build();
    }

    private MySQLTempPunchingDetails createTempPunchingDetails(MSSQLDeviceLogs log, LocalDateTime currentTime) {
        return MySQLTempPunchingDetails.builder()
                .empCode(log.getUserId())
                .punchingDate(log.getLogDate())
                .punchingMode(log.getDirection())
                .entryDate(currentTime)
                .deviceName(String.valueOf(log.getDeviceLogId()))
                .deviceLogId(log.getDeviceLogId())
                .locationName(log.getLocationAddress())
                .processedFlag("N")
                .build();
    }
}

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DataSyncService {
//    private final MSSQLService mssqlService;
//    private final MySQLPunchingRepository mysqlRepository;
//    private final MySQLTempPunchingRepository mysqlTempRepository;
//
//    @Autowired
//    @Qualifier("mssqlTransactionTemplate")
//    private TransactionTemplate mssqlTransactionTemplate;
//
//    @Autowired
//    @Qualifier("mysqlTransactionTemplate")
//    private TransactionTemplate mysqlTransactionTemplate;
//
//    @Autowired
//    @Qualifier("mysqlTempTransactionTemplate")
//    private TransactionTemplate mysqlTempTransactionTemplate;
//
//    @Scheduled(fixedRateString = "${sync.interval.milliseconds}")
//    public void syncData() {
//        try {
//            log.info("Starting data synchronization process");
//            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
//
//            Set<Integer> processedIds = mysqlRepository.findProcessedDeviceLogIds(startOfDay);
//            log.info("Found {} processed records for today", processedIds.size());
//
//            List<MSSQLDeviceLogs> unprocessedLogs = mssqlTransactionTemplate.execute(status -> {
//                return mssqlService.getUnprocessedLogs();
//            });
//
//            if (unprocessedLogs != null && !unprocessedLogs.isEmpty()) {
//                log.info("Found {} unprocessed logs to sync", unprocessedLogs.size());
//                int chunkSize = 1000;
//                for (int i = 0; i < unprocessedLogs.size(); i += chunkSize) {
//                    int end = Math.min(i + chunkSize, unprocessedLogs.size());
//                    List<MSSQLDeviceLogs> chunk = unprocessedLogs.subList(i, end);
//
//                    mysqlTransactionTemplate.execute(status -> {
//                        processUnprocessedLogs(chunk, processedIds);
//                        return null;
//                    });
//                }
//            } else {
//                log.info("No new logs to process");
//            }
//        } catch (Exception e) {
//            log.error("Error during sync: ", e);
//            throw e;
//        }
//    }
//
//    protected void processUnprocessedLogs(List<MSSQLDeviceLogs> unprocessedLogs, Set<Integer> processedIds) {
//        log.info("Processing {} unprocessed logs", unprocessedLogs.size());
//        LocalDateTime currentTime = LocalDateTime.now();
//        List<MySQLPunchingDetails> batchToInsert = new ArrayList<>();
//        List<MySQLTempPunchingDetails> tempBatchToInsert = new ArrayList<>();
//        int batchSize = 50;
//        int totalProcessed = 0;
//
//        for (MSSQLDeviceLogs mssqlLog : unprocessedLogs) {
//            if (!processedIds.contains(mssqlLog.getDeviceLogId())) {
//                // Create main DB entity
//                MySQLPunchingDetails punchingDetails = MySQLPunchingDetails.builder()
//                        .empCode(mssqlLog.getUserId())
//                        .punchingDate(mssqlLog.getLogDate())
//                        .punchingMode(mssqlLog.getDirection())
//                        .entryDate(currentTime)
//                        .deviceName(String.valueOf(mssqlLog.getDeviceLogId()))
//                        .deviceLogId(mssqlLog.getDeviceLogId())
//                        .locationName(mssqlLog.getLocationAddress())
//                        .processedFlag("N")
//                        .build();
//
//                // Create temp DB entity
//                MySQLTempPunchingDetails tempPunchingDetails = MySQLTempPunchingDetails.builder()
//                        .empCode(mssqlLog.getUserId())
//                        .punchingDate(mssqlLog.getLogDate())
//                        .punchingMode(mssqlLog.getDirection())
//                        .entryDate(currentTime)
//                        .deviceName(String.valueOf(mssqlLog.getDeviceLogId()))
//                        .deviceLogId(mssqlLog.getDeviceLogId())
//                        .locationName(mssqlLog.getLocationAddress())
//                        .processedFlag("N")
//                        .build();
//
//                batchToInsert.add(punchingDetails);
//                tempBatchToInsert.add(tempPunchingDetails);
//
//                if (batchToInsert.size() >= batchSize) {
//                    mysqlRepository.saveAll(batchToInsert);
//                    mysqlTempRepository.saveAll(tempBatchToInsert);
//                    totalProcessed += batchToInsert.size();
//                    batchToInsert.clear();
//                    tempBatchToInsert.clear();
//                }
//            }
//        }
//
//        if (!batchToInsert.isEmpty()) {
//            mysqlRepository.saveAll(batchToInsert);
//            mysqlTempRepository.saveAll(tempBatchToInsert);
//            totalProcessed += batchToInsert.size();
//        }
//
//        log.info("Successfully synced {} records to both databases", totalProcessed);
//    }
//}

