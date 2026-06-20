package com.stockadmin.selection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stock-admin.selection")
public class SelectionProperties
{
    private int defaultLimit = 100;

    public int getDefaultLimit()
    {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit)
    {
        this.defaultLimit = defaultLimit;
    }
}
