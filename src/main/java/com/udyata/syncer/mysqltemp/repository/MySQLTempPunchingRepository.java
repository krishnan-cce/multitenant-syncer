package com.udyata.syncer.mysqltemp.repository;

import com.udyata.syncer.mysql.entity.MySQLPunchingDetails;
import com.udyata.syncer.mysqltemp.entity.MySQLTempPunchingDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MySQLTempPunchingRepository extends JpaRepository<MySQLTempPunchingDetails, Long> {
}
