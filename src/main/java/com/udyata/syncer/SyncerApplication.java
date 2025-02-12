package com.udyata.syncer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SyncerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SyncerApplication.class, args);
	}

}


//# Create MySQL container
//docker run --name mysql-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=leo -p 3306:3306 -d mysql:8.0
//
//# Create SQL Server container
//docker run --name mssql-db -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=YourStrong@Passw0rd" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2019-latest