package com.stockadmin.selection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class StockQuote60DataSourceConfig
{
    @Bean(name = "stock60MinQuoteJdbcTemplate")
    public JdbcTemplate stock60MinQuoteJdbcTemplate(
            @Value("${stock-admin.quote-60.datasource.driver-class-name:org.sqlite.JDBC}") String driverClassName,
            @Value("${stock-admin.quote-60.datasource.url:jdbc:sqlite:/root/data/stock_quote_60_cnfin.db}") String jdbcUrl)
    {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(jdbcUrl);
        return new JdbcTemplate(dataSource);
    }
}
