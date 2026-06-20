package com.stockadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.stockadmin.selection.mapper")
public class StockAdminApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(StockAdminApplication.class, args);
    }
}
