package com.udyata.syncer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Autowired
    @Qualifier("mssqlTransactionManager")
    private PlatformTransactionManager mssqlTransactionManager;

    @Autowired
    @Qualifier("mysqlTransactionManager")
    private PlatformTransactionManager mysqlTransactionManager;

    @Autowired(required = false)
    @Qualifier("mysqlTempTransactionManager")
    private PlatformTransactionManager mysqlTempTransactionManager;

    @Bean(name = "mssqlTransactionTemplate")
    public TransactionTemplate mssqlTransactionTemplate() {
        TransactionTemplate template = new TransactionTemplate(mssqlTransactionManager);
        template.setReadOnly(true);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(30); // 30 seconds timeout
        return template;
    }

    @Bean(name = "mysqlTransactionTemplate")
    public TransactionTemplate mysqlTransactionTemplate() {
        TransactionTemplate template = new TransactionTemplate(mysqlTransactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(30);
        return template;
    }

    @Bean(name = "mysqlTempTransactionTemplate")
    @ConditionalOnBean(name = "mysqlTempTransactionManager")
    public TransactionTemplate mysqlTempTransactionTemplate() {
        TransactionTemplate template = new TransactionTemplate(mysqlTempTransactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(30);
        return template;
    }
}
