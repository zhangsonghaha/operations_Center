package com.ruoyi.web.core.config;

import org.flowable.app.spring.SpringAppEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Flowable configuration to avoid encoding issues or other defaults
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Autowired
    @Qualifier("masterDataSource")
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        engineConfiguration.setActivityFontName("宋体");
        engineConfiguration.setLabelFontName("宋体");
        engineConfiguration.setAnnotationFontName("宋体");
        
        // Use master data source explicitly to ensure tables are created in the right DB
        engineConfiguration.setDataSource(dataSource);
        engineConfiguration.setTransactionManager(transactionManager);
        
        // Ensure schema update is true
        // Set to false to avoid duplicate column errors if tables already exist but version check fails
        // engineConfiguration.setDatabaseSchemaUpdate("true");
        engineConfiguration.setDatabaseSchemaUpdate("false");
    }

    /*
     * Explicitly configure AppEngine to fix NPE or initialization issues
     * However, in Spring Boot Starter, AppEngine is auto-configured if enabled.
     * Since we disabled app engine in properties, we should remove this bean to avoid conflicts.
     */
    // @Bean
    // public SpringAppEngineConfiguration springAppEngineConfiguration() {
    //     SpringAppEngineConfiguration configuration = new SpringAppEngineConfiguration();
    //     configuration.setDataSource(dataSource);
    //     configuration.setTransactionManager(transactionManager);
    //     configuration.setDatabaseSchemaUpdate("true");
    //     return configuration;
    // }
}
