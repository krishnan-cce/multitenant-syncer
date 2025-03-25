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
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class DataSyncService {
    private final MSSQLService mssqlService;
    private final MySQLPunchingRepository mysqlRepository;
    private final MySQLTempPunchingRepository mysqlTempRepository;
    private final boolean tempDbEnabled;

    @Autowired
    public DataSyncService(
            MSSQLService mssqlService,
            MySQLPunchingRepository mysqlRepository,
            @Autowired(required = false) MySQLTempPunchingRepository mysqlTempRepository,
            @Value("${spring.mysql-temp.enabled:false}") boolean tempDbEnabled) {
        this.mssqlService = mssqlService;
        this.mysqlRepository = mysqlRepository;
        this.mysqlTempRepository = mysqlTempRepository;
        this.tempDbEnabled = tempDbEnabled;

        log.info("Data Sync Service initialized with tempDbEnabled: {}", tempDbEnabled);
    }

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
        List<MySQLTempPunchingDetails> tempBatchToInsert = tempDbEnabled ? new ArrayList<>() : null;
        int batchSize = 50;

        for (MSSQLDeviceLogs mssqlLog : unprocessedLogs) {
            if (!processedIds.contains(mssqlLog.getDeviceLogId())) {
                batchToInsert.add(createPunchingDetails(mssqlLog, currentTime));

                if (tempDbEnabled) {
                    tempBatchToInsert.add(createTempPunchingDetails(mssqlLog, currentTime));
                }

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

        if (tempDbEnabled && tempBatch != null && !tempBatch.isEmpty()) {
            mysqlTempRepository.saveAll(tempBatch);
            tempBatch.clear();
        }

        log.debug("Saved batch of {} records", batch.size());
        batch.clear();
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



