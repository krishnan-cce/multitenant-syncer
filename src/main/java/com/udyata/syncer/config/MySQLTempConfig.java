package com.udyata.syncer.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@ConditionalOnProperty(prefix = "spring", name = "mysql-temp.enabled", havingValue = "true", matchIfMissing = false)
@EnableJpaRepositories(
        basePackages = "com.udyata.syncer.mysqltemp.repository",
        entityManagerFactoryRef = "mysqlTempEntityManager",
        transactionManagerRef = "mysqlTempTransactionManager"
)
public class MySQLTempConfig {
    @Bean(name = "mysqlTempDataSource")
    @ConfigurationProperties(prefix = "spring.mysql-temp.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "mysqlTempEntityManager")
    public LocalContainerEntityManagerFactoryBean entityManager(
            @Qualifier("mysqlTempDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.udyata.syncer.mysqltemp.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "mysqlTempTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("mysqlTempEntityManager") LocalContainerEntityManagerFactoryBean entityManager) {
        return new JpaTransactionManager(entityManager.getObject());
    }
}