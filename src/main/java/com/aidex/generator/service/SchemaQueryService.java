package com.aidex.generator.service;

import com.aidex.generator.domain.TableMeta;
import java.util.List;

public interface SchemaQueryService
{
    List<String> listDataSources();

    List<TableMeta> listTables(String dataSourceName);

    TableMeta loadTable(String dataSourceName, String tableName);
}
