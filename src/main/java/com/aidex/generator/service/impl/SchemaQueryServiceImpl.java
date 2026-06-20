package com.aidex.generator.service.impl;

import com.aidex.common.exception.ServiceException;
import com.aidex.common.utils.StringUtils;
import com.aidex.framework.config.properties.AidexDataSourceProperties;
import com.aidex.framework.datasource.DynamicDataSourceContextHolder;
import com.aidex.generator.domain.ColumnMeta;
import com.aidex.generator.domain.TableMeta;
import com.aidex.generator.service.SchemaQueryService;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class SchemaQueryServiceImpl implements SchemaQueryService
{
    private final DataSource dataSource;

    private final AidexDataSourceProperties properties;

    public SchemaQueryServiceImpl(DataSource dataSource, AidexDataSourceProperties properties)
    {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    @Override
    public List<String> listDataSources()
    {
        List<String> result = new ArrayList<String>();
        for (String key : properties.getSources().keySet())
        {
            if (properties.getSources().get(key).isEnabled())
            {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public List<TableMeta> listTables(String dataSourceName)
    {
        return executeOn(dataSourceName, new SchemaExecutor<List<TableMeta>>()
        {
            @Override
            public List<TableMeta> execute(Connection connection) throws Exception
            {
                DatabaseMetaData metaData = connection.getMetaData();
                if (isSqlite(metaData))
                {
                    return listSqliteTables(connection, dataSourceName);
                }
                List<TableMeta> tables = new ArrayList<TableMeta>();
                ResultSet rs = metaData.getTables(connection.getCatalog(), null, "%", new String[] { "TABLE" });
                while (rs.next())
                {
                    TableMeta table = new TableMeta();
                    table.setDatasourceName(dataSourceName);
                    table.setTableName(rs.getString("TABLE_NAME"));
                    table.setTableComment(StringUtils.defaultString(rs.getString("REMARKS"), rs.getString("TABLE_NAME")));
                    tables.add(table);
                }
                rs.close();
                Collections.sort(tables, new Comparator<TableMeta>()
                {
                    @Override
                    public int compare(TableMeta left, TableMeta right)
                    {
                        return left.getTableName().compareToIgnoreCase(right.getTableName());
                    }
                });
                return tables;
            }
        });
    }

    @Override
    public TableMeta loadTable(String dataSourceName, final String tableName)
    {
        return executeOn(dataSourceName, new SchemaExecutor<TableMeta>()
        {
            @Override
            public TableMeta execute(Connection connection) throws Exception
            {
                DatabaseMetaData metaData = connection.getMetaData();
                if (isSqlite(metaData))
                {
                    return loadSqliteTable(connection, dataSourceName, tableName);
                }
                TableMeta table = new TableMeta();
                table.setDatasourceName(dataSourceName);
                table.setTableName(tableName);
                ResultSet tables = metaData.getTables(connection.getCatalog(), null, tableName, new String[] { "TABLE" });
                if (tables.next())
                {
                    table.setTableComment(StringUtils.defaultString(tables.getString("REMARKS"), tableName));
                }
                tables.close();

                Set<String> pkColumns = new HashSet<String>();
                ResultSet primaryKeys = metaData.getPrimaryKeys(connection.getCatalog(), null, tableName);
                while (primaryKeys.next())
                {
                    pkColumns.add(primaryKeys.getString("COLUMN_NAME"));
                }
                primaryKeys.close();

                ResultSet columns = metaData.getColumns(connection.getCatalog(), null, tableName, "%");
                while (columns.next())
                {
                    ColumnMeta column = new ColumnMeta();
                    column.setColumnName(columns.getString("COLUMN_NAME"));
                    column.setColumnComment(StringUtils.defaultString(columns.getString("REMARKS"), column.getColumnName()));
                    column.setDataType(columns.getString("TYPE_NAME"));
                    column.setJdbcType(columns.getString("TYPE_NAME"));
                    column.setJavaField(resolveJavaField(column.getColumnName()));
                    column.setAttrName(StringUtils.upperFirst(column.getJavaField()));
                    column.setJavaType(resolveJavaType(column.getDataType()));
                    column.setPrimaryKey(pkColumns.contains(column.getColumnName()));
                    column.setAutoIncrement("YES".equalsIgnoreCase(columns.getString("IS_AUTOINCREMENT")));
                    column.setNullable(columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    table.getColumns().add(column);
                }
                columns.close();
                return table;
            }
        });
    }

    private String resolveJavaType(String jdbcType)
    {
        String type = StringUtils.lowerCase(StringUtils.defaultString(jdbcType));
        if (type.contains("bigint"))
        {
            return "Long";
        }
        if (type.contains("tinyint") || type.contains("smallint") || type.contains("int") || type.contains("integer"))
        {
            return "Integer";
        }
        if (type.contains("decimal") || type.contains("numeric"))
        {
            return "BigDecimal";
        }
        if (type.contains("timestamp") || type.contains("datetime") || type.contains("time"))
        {
            return "LocalDateTime";
        }
        if (type.contains("date"))
        {
            return "LocalDate";
        }
        if (type.contains("double"))
        {
            return "Double";
        }
        if (type.contains("float"))
        {
            return "Float";
        }
        if (type.contains("bool"))
        {
            return "Boolean";
        }
        return "String";
    }

    private boolean isSqlite(DatabaseMetaData metaData) throws Exception
    {
        return StringUtils.containsIgnoreCase(metaData.getDatabaseProductName(), "sqlite");
    }

    private List<TableMeta> listSqliteTables(Connection connection, String dataSourceName) throws Exception
    {
        List<TableMeta> tables = new ArrayList<TableMeta>();
        PreparedStatement ps = connection.prepareStatement(
                "select name from sqlite_master where type = 'table' and name not like 'sqlite_%' order by name");
        ResultSet rs = ps.executeQuery();
        while (rs.next())
        {
            TableMeta table = new TableMeta();
            table.setDatasourceName(dataSourceName);
            table.setTableName(rs.getString("name"));
            table.setTableComment(rs.getString("name"));
            tables.add(table);
        }
        rs.close();
        ps.close();
        return tables;
    }

    private TableMeta loadSqliteTable(Connection connection, String dataSourceName, String tableName) throws Exception
    {
        TableMeta table = new TableMeta();
        table.setDatasourceName(dataSourceName);
        table.setTableName(tableName);
        table.setTableComment(tableName);
        PreparedStatement ps = connection.prepareStatement("pragma table_info('" + tableName + "')");
        ResultSet rs = ps.executeQuery();
        while (rs.next())
        {
            ColumnMeta column = new ColumnMeta();
            column.setColumnName(rs.getString("name"));
            column.setColumnComment(rs.getString("name"));
            column.setDataType(rs.getString("type"));
            column.setJdbcType(rs.getString("type"));
            column.setJavaField(resolveJavaField(column.getColumnName()));
            column.setAttrName(StringUtils.upperFirst(column.getJavaField()));
            column.setJavaType(resolveJavaType(column.getDataType()));
            column.setPrimaryKey(rs.getInt("pk") == 1);
            column.setAutoIncrement(column.isPrimaryKey() && isSqliteIntegerType(column.getDataType()));
            column.setNullable(rs.getInt("notnull") == 0);
            table.getColumns().add(column);
        }
        rs.close();
        ps.close();
        return table;
    }

    private <T> T executeOn(String dataSourceName, SchemaExecutor<T> executor)
    {
        if (!properties.getSources().containsKey(dataSourceName))
        {
            throw new ServiceException("Unknown datasource: " + dataSourceName);
        }
        DynamicDataSourceContextHolder.setDataSourceType(dataSourceName);
        try
        {
            Connection connection = dataSource.getConnection();
            try
            {
                return executor.execute(connection);
            }
            finally
            {
                connection.close();
            }
        }
        catch (Exception ex)
        {
            throw new ServiceException("Failed to inspect datasource " + dataSourceName, ex);
        }
        finally
        {
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }

    private interface SchemaExecutor<T>
    {
        T execute(Connection connection) throws Exception;
    }

    private String resolveJavaField(String columnName)
    {
        String normalized = StringUtils.lowerCase(StringUtils.defaultString(columnName));
        if (isCreateAuditColumn(normalized))
        {
            return "createTime";
        }
        if (isUpdateAuditColumn(normalized))
        {
            return "updateTime";
        }
        return StringUtils.toCamelCase(columnName);
    }

    private boolean isCreateAuditColumn(String columnName)
    {
        return "create_time".equals(columnName)
                || "create_at".equals(columnName)
                || "created_at".equals(columnName)
                || "gmt_create".equals(columnName);
    }

    private boolean isUpdateAuditColumn(String columnName)
    {
        return "update_time".equals(columnName)
                || "update_at".equals(columnName)
                || "updated_at".equals(columnName)
                || "gmt_modified".equals(columnName)
                || "modify_time".equals(columnName);
    }

    private boolean isSqliteIntegerType(String dataType)
    {
        String type = StringUtils.lowerCase(StringUtils.defaultString(dataType));
        return type.contains("int");
    }
}
