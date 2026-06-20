package com.aidex.generator.domain;

import java.util.ArrayList;
import java.util.List;

public class TableMeta
{
    private String tableName;

    private String tableComment;

    private String className;

    private String businessName;

    private String datasourceName;

    private List<ColumnMeta> columns = new ArrayList<ColumnMeta>();

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getTableComment()
    {
        return tableComment;
    }

    public void setTableComment(String tableComment)
    {
        this.tableComment = tableComment;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getBusinessName()
    {
        return businessName;
    }

    public void setBusinessName(String businessName)
    {
        this.businessName = businessName;
    }

    public String getDatasourceName()
    {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName)
    {
        this.datasourceName = datasourceName;
    }

    public List<ColumnMeta> getColumns()
    {
        return columns;
    }

    public void setColumns(List<ColumnMeta> columns)
    {
        this.columns = columns;
    }
}
