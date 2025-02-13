package com.udyata.syncer.mysql.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.*;

import java.time.LocalDateTime;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_punching_dtls")
public class MySQLPunchingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "emp_code")
    private String empCode;

    @Column(name = "punching_date")
    private LocalDateTime punchingDate;

    @Column(name = "punching_mode")
    private String punchingMode;

    @Column(name = "entry_date")
    private LocalDateTime entryDate;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "processed_flg")
    private String processedFlag;

    @Column(name = "device_log_id")
    private Integer deviceLogId;
}
