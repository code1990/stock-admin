package com.aidex.framework.config.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aidex.generator")
public class GeneratorProperties
{
    private String outputRoot;

    private String defaultPackage;

    private String defaultModule;

    private String defaultAuthor;

    private boolean autoRemovePrefix = true;

    private List<String> tablePrefixes = new ArrayList<String>();

    private AutoGenerate autoGenerate = new AutoGenerate();

    public String getOutputRoot()
    {
        return outputRoot;
    }

    public void setOutputRoot(String outputRoot)
    {
        this.outputRoot = outputRoot;
    }

    public String getDefaultPackage()
    {
        return defaultPackage;
    }

    public void setDefaultPackage(String defaultPackage)
    {
        this.defaultPackage = defaultPackage;
    }

    public String getDefaultModule()
    {
        return defaultModule;
    }

    public void setDefaultModule(String defaultModule)
    {
        this.defaultModule = defaultModule;
    }

    public String getDefaultAuthor()
    {
        return defaultAuthor;
    }

    public void setDefaultAuthor(String defaultAuthor)
    {
        this.defaultAuthor = defaultAuthor;
    }

    public boolean isAutoRemovePrefix()
    {
        return autoRemovePrefix;
    }

    public void setAutoRemovePrefix(boolean autoRemovePrefix)
    {
        this.autoRemovePrefix = autoRemovePrefix;
    }

    public List<String> getTablePrefixes()
    {
        return tablePrefixes;
    }

    public void setTablePrefixes(List<String> tablePrefixes)
    {
        this.tablePrefixes = tablePrefixes;
    }

    public AutoGenerate getAutoGenerate()
    {
        return autoGenerate;
    }

    public void setAutoGenerate(AutoGenerate autoGenerate)
    {
        this.autoGenerate = autoGenerate;
    }

    public static class AutoGenerate
    {
        private boolean enabled;

        private String datasource;

        private String tables;

        private String packageName;

        private String moduleName;

        private String author;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getDatasource()
        {
            return datasource;
        }

        public void setDatasource(String datasource)
        {
            this.datasource = datasource;
        }

        public String getTables()
        {
            return tables;
        }

        public void setTables(String tables)
        {
            this.tables = tables;
        }

        public String getPackageName()
        {
            return packageName;
        }

        public void setPackageName(String packageName)
        {
            this.packageName = packageName;
        }

        public String getModuleName()
        {
            return moduleName;
        }

        public void setModuleName(String moduleName)
        {
            this.moduleName = moduleName;
        }

        public String getAuthor()
        {
            return author;
        }

        public void setAuthor(String author)
        {
            this.author = author;
        }
    }
}
