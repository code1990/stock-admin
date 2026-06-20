package com.aidex.framework.config.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aidex.datasource")
public class AidexDataSourceProperties
{
    private String primary;

    private Map<String, DataSourceItem> sources = new LinkedHashMap<String, DataSourceItem>();

    public String getPrimary()
    {
        return primary;
    }

    public void setPrimary(String primary)
    {
        this.primary = primary;
    }

    public Map<String, DataSourceItem> getSources()
    {
        return sources;
    }

    public void setSources(Map<String, DataSourceItem> sources)
    {
        this.sources = sources;
    }

    public static class DataSourceItem
    {
        private boolean enabled = true;

        private String driverClassName;

        private String jdbcUrl;

        private String username;

        private String password;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getDriverClassName()
        {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName)
        {
            this.driverClassName = driverClassName;
        }

        public String getJdbcUrl()
        {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl)
        {
            this.jdbcUrl = jdbcUrl;
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }
    }
}
