package com.aidex.generator.service.impl;

import com.aidex.common.exception.ServiceException;
import com.aidex.common.utils.StringUtils;
import com.aidex.framework.config.properties.GeneratorProperties;
import com.aidex.generator.domain.ColumnMeta;
import com.aidex.generator.domain.GeneratedFile;
import com.aidex.generator.domain.GeneratorRequest;
import com.aidex.generator.domain.TableMeta;
import com.aidex.generator.service.GeneratorService;
import com.aidex.generator.service.SchemaQueryService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

@Service
public class GeneratorServiceImpl implements GeneratorService
{
    private final SchemaQueryService schemaQueryService;

    private final GeneratorProperties properties;

    private final VelocityEngine velocityEngine;

    public GeneratorServiceImpl(SchemaQueryService schemaQueryService, GeneratorProperties properties, VelocityEngine velocityEngine)
    {
        this.schemaQueryService = schemaQueryService;
        this.properties = properties;
        this.velocityEngine = velocityEngine;
    }

    @Override
    public List<GeneratedFile> preview(GeneratorRequest request)
    {
        return buildGeneratedFiles(request);
    }

    @Override
    public List<GeneratedFile> generate(GeneratorRequest request)
    {
        try
        {
            List<GeneratedFile> files = buildGeneratedFiles(request);
            writeGeneratedFiles(files);
            return files;
        }
        catch (Exception ex)
        {
            throw new ServiceException("Failed to generate code files", ex);
        }
    }

    @Override
    public byte[] generateZip(GeneratorRequest request)
    {
        try
        {
            List<GeneratedFile> files = generate(request);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(outputStream, StandardCharsets.UTF_8);
            for (GeneratedFile file : files)
            {
                zip.putNextEntry(new ZipEntry(file.getFileName()));
                OutputStreamWriter writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
                writer.write(file.getContent());
                writer.flush();
                zip.closeEntry();
            }
            zip.finish();
            zip.close();
            return outputStream.toByteArray();
        }
        catch (Exception ex)
        {
            throw new ServiceException("Failed to generate code zip", ex);
        }
    }

    private GeneratedFile render(String templatePath, String fileName, VelocityContext context)
    {
        Template template = velocityEngine.getTemplate(templatePath, StandardCharsets.UTF_8.name());
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return new GeneratedFile(fileName, writer.toString());
    }

    private VelocityContext context(TableMeta table, GeneratorRequest request)
    {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("packageName", resolvePackage(request));
        values.put("moduleName", resolveModule(request));
        values.put("ClassName", table.getClassName());
        values.put("className", StringUtils.uncapitalize(table.getClassName()));
        values.put("businessName", table.getBusinessName());
        values.put("tableName", table.getTableName());
        values.put("functionName", StringUtils.defaultIfBlank(table.getTableComment(), table.getClassName()));
        values.put("author", resolveAuthor(request));
        values.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        values.put("columns", table.getColumns());
        values.put("entityColumns", filterColumns(table, ColumnMode.ENTITY));
        values.put("insertColumns", filterColumns(table, ColumnMode.INSERT));
        values.put("updateColumns", filterColumns(table, ColumnMode.UPDATE));
        values.put("queryColumns", filterColumns(table, ColumnMode.QUERY));
        values.put("pkColumn", primaryColumn(table));
        values.put("autoIncrementPkColumn", autoIncrementPrimaryColumn(table));
        values.put("pkParamName", primaryColumn(table).getJavaField());
        values.put("table", table);
        values.put("hasUpdatableColumns", !filterColumns(table, ColumnMode.UPDATE).isEmpty());
        values.put("hasInsertableColumns", !filterColumns(table, ColumnMode.INSERT).isEmpty());
        values.put("hasBigDecimal", hasJavaType(table, "BigDecimal"));
        values.put("hasLocalDate", hasJavaType(table, "LocalDate"));
        values.put("hasLocalDateTime", hasJavaType(table, "LocalDateTime"));
        values.put("hasBoolean", hasJavaType(table, "Boolean"));
        return new VelocityContext(values);
    }

    private TableMeta buildTable(GeneratorRequest request)
    {
        TableMeta table = schemaQueryService.loadTable(request.getDatasource(), request.getTableName());
        String normalized = table.getTableName();
        if (properties.isAutoRemovePrefix())
        {
            normalized = StringUtils.removePrefix(normalized, properties.getTablePrefixes());
        }
        table.setClassName(StringUtils.toCapitalizeCamelCase(normalized));
        table.setBusinessName(StringUtils.defaultIfBlank(request.getBusinessName(), StringUtils.toCamelCase(normalized)));
        for (ColumnMeta column : table.getColumns())
        {
            if (StringUtils.isBlank(column.getJavaField()))
            {
                column.setJavaField(StringUtils.toCamelCase(column.getColumnName()));
            }
            column.setAttrName(StringUtils.upperFirst(column.getJavaField()));
        }
        return table;
    }

    private ColumnMeta primaryColumn(TableMeta table)
    {
        for (ColumnMeta column : table.getColumns())
        {
            if (column.isPrimaryKey())
            {
                return column;
            }
        }
        if (table.getColumns().isEmpty())
        {
            throw new ServiceException("No columns found for table " + table.getTableName());
        }
        return table.getColumns().get(0);
    }

    private ColumnMeta autoIncrementPrimaryColumn(TableMeta table)
    {
        for (ColumnMeta column : table.getColumns())
        {
            if (column.isPrimaryKey() && column.isAutoIncrement())
            {
                return column;
            }
        }
        return null;
    }

    private String resolvePackage(GeneratorRequest request)
    {
        return StringUtils.defaultIfBlank(request.getPackageName(), properties.getDefaultPackage());
    }

    private String resolveModule(GeneratorRequest request)
    {
        return StringUtils.defaultIfBlank(request.getModuleName(), properties.getDefaultModule());
    }

    private String resolveAuthor(GeneratorRequest request)
    {
        return StringUtils.defaultIfBlank(request.getAuthor(), properties.getDefaultAuthor());
    }

    private String buildFileName(TableMeta table, GeneratorRequest request, String type)
    {
        String base = "generated/" + resolveModule(request) + "/src/main/java/" + resolvePackage(request).replace('.', '/');
        if ("domain".equals(type))
        {
            return base + "/domain/" + table.getClassName() + ".java";
        }
        if ("mapper".equals(type))
        {
            return base + "/mapper/" + table.getClassName() + "Mapper.java";
        }
        if ("service".equals(type))
        {
            return base + "/service/" + table.getClassName() + "Service.java";
        }
        if ("serviceImpl".equals(type))
        {
            return base + "/service/impl/" + table.getClassName() + "ServiceImpl.java";
        }
        return base + "/controller/" + table.getClassName() + "Controller.java";
    }

    private String buildMapperXmlName(TableMeta table, GeneratorRequest request)
    {
        return "generated/" + resolveModule(request) + "/src/main/resources/mapper/" + table.getClassName() + "Mapper.xml";
    }

    private boolean hasJavaType(TableMeta table, String javaType)
    {
        for (ColumnMeta column : table.getColumns())
        {
            if (!isSuperColumn(column) && javaType.equals(column.getJavaType()))
            {
                return true;
            }
        }
        return false;
    }

    private List<ColumnMeta> filterColumns(TableMeta table, ColumnMode mode)
    {
        List<ColumnMeta> filtered = new ArrayList<ColumnMeta>();
        for (ColumnMeta column : table.getColumns())
        {
            if (mode == ColumnMode.ENTITY && isSuperColumn(column))
            {
                continue;
            }
            if (mode == ColumnMode.INSERT && column.isAutoIncrement() && column.isPrimaryKey())
            {
                continue;
            }
            if (mode == ColumnMode.QUERY && isSuperColumn(column))
            {
                continue;
            }
            if (mode == ColumnMode.UPDATE && (column.isPrimaryKey() || isCreateAuditColumn(column)))
            {
                continue;
            }
            filtered.add(column);
        }
        return filtered;
    }

    private boolean isSuperColumn(ColumnMeta column)
    {
        String javaField = column.getJavaField();
        return "createTime".equals(javaField) || "updateTime".equals(javaField);
    }

    private boolean isCreateAuditColumn(ColumnMeta column)
    {
        return "createTime".equals(column.getJavaField());
    }

    private List<GeneratedFile> buildGeneratedFiles(GeneratorRequest request)
    {
        TableMeta table = buildTable(request);
        VelocityContext context = context(table, request);
        List<GeneratedFile> files = new ArrayList<GeneratedFile>();
        files.add(render("templates/generator/backend/domain.java.vm", buildFileName(table, request, "domain"), context));
        files.add(render("templates/generator/backend/mapper.java.vm", buildFileName(table, request, "mapper"), context));
        files.add(render("templates/generator/backend/mapper.xml.vm", buildMapperXmlName(table, request), context));
        files.add(render("templates/generator/backend/service.java.vm", buildFileName(table, request, "service"), context));
        files.add(render("templates/generator/backend/serviceImpl.java.vm", buildFileName(table, request, "serviceImpl"), context));
        files.add(render("templates/generator/backend/controller.java.vm", buildFileName(table, request, "controller"), context));
        return files;
    }

    private void writeGeneratedFiles(List<GeneratedFile> files) throws Exception
    {
        File root = new File(properties.getOutputRoot());
        if (!root.exists() && !root.mkdirs())
        {
            throw new ServiceException("Unable to create output directory: " + root.getAbsolutePath());
        }
        for (GeneratedFile file : files)
        {
            File target = new File(root, file.getFileName());
            File parent = target.getParentFile();
            if (!parent.exists() && !parent.mkdirs())
            {
                throw new ServiceException("Unable to create directory: " + parent.getAbsolutePath());
            }
            FileOutputStream out = new FileOutputStream(target);
            try
            {
                out.write(file.getContent().getBytes(StandardCharsets.UTF_8));
            }
            finally
            {
                out.close();
            }
        }
    }

    private enum ColumnMode
    {
        ENTITY,
        INSERT,
        UPDATE,
        QUERY
    }
}
