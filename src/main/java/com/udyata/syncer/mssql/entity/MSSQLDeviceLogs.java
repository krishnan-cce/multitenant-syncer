package com.udyata.syncer.mssql.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "DeviceLogs", schema = "dbo") // This will be handled dynamically in the repository
public class MSSQLDeviceLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DeviceLogId")
    private Integer deviceLogId;

    @Column(name = "DownloadDate")
    private LocalDateTime downloadDate;

    @Column(name = "DeviceId")
    private Integer deviceId;

    @Column(name = "UserId", nullable = false)
    private String userId;

    @Column(name = "LogDate", nullable = false)
    private LocalDateTime logDate;

    @Column(name = "Direction")
    private String direction;

    @Column(name = "AttDirection")
    private String attDirection;

    @Column(name = "C1")
    private String c1;

    @Column(name = "C2")
    private String c2;

    @Column(name = "C3")
    private String c3;

    @Column(name = "C4")
    private String c4;

    @Column(name = "C5")
    private String c5;

    @Column(name = "C6")
    private String c6;

    @Column(name = "C7")
    private String c7;

    @Column(name = "WorkCode")
    private String workCode;

    @Column(name = "UpdateFlag")
    private Integer updateFlag;

    @Column(name = "FileName")
    private String fileName;

    @Column(name = "Longitude")
    private String longitude;

    @Column(name = "Latitude")
    private String latitude;

    @Column(name = "IsApproved")
    private Integer isApproved;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "LastModifiedDate")
    private LocalDateTime lastModifiedDate;

    @Column(name = "LocationAddress")
    private String locationAddress;

    @Column(name = "BodyTemperature")
    private Double bodyTemperature;

    @Column(name = "IsMaskOn")
    private Integer isMaskOn;
}

