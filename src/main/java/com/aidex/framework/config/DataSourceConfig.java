package com.aidex.framework.config;

import com.aidex.common.exception.ServiceException;
import com.aidex.framework.config.properties.AidexDataSourceProperties;
import com.aidex.framework.config.properties.AidexDataSourceProperties.DataSourceItem;
import com.aidex.framework.datasource.DynamicDataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableConfigurationProperties({ AidexDataSourceProperties.class })
public class DataSourceConfig
{
    @Bean
    public DataSource dataSource(AidexDataSourceProperties properties)
    {
        Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
        DataSource defaultDataSource = null;
        for (Map.Entry<String, DataSourceItem> entry : properties.getSources().entrySet())
        {
            if (!entry.getValue().isEnabled())
            {
                continue;
            }
            HikariDataSource dataSource = build(entry.getValue());
            targetDataSources.put(entry.getKey(), dataSource);
            if (entry.getKey().equals(properties.getPrimary()))
            {
                defaultDataSource = dataSource;
            }
        }
        if (defaultDataSource == null)
        {
            throw new ServiceException("Primary datasource not found: " + properties.getPrimary());
        }
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        return dynamicDataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DatabaseIdProvider databaseIdProvider()
    {
        VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("MySQL", "mysql");
        properties.setProperty("SQLite", "sqlite");
        provider.setProperties(properties);
        return provider;
    }

    private HikariDataSource build(DataSourceItem item)
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(item.getDriverClassName());
        dataSource.setJdbcUrl(item.getJdbcUrl());
        dataSource.setUsername(item.getUsername());
        dataSource.setPassword(item.getPassword());
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setPoolName("aidex-" + item.getDriverClassName());
        return dataSource;
    }
}
