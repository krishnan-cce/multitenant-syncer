package com.udyata.syncer.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class TableNameStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if (name.getText().startsWith("DeviceLogs")) {
            LocalDateTime now = LocalDateTime.now();
            return new Identifier(
                    String.format("DeviceLogs_%d_%d",
                            now.getMonthValue(),
                            now.getYear()),
                    name.isQuoted()
            );
        }
        return super.toPhysicalTableName(name, context);
    }
}