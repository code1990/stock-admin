package com.aidex.generator.runner;

import com.aidex.common.utils.StringUtils;
import com.aidex.framework.config.properties.GeneratorProperties;
import com.aidex.framework.config.properties.GeneratorProperties.AutoGenerate;
import com.aidex.generator.domain.GeneratorRequest;
import com.aidex.generator.service.GeneratorService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AutoGenerateRunner implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(AutoGenerateRunner.class);

    private final GeneratorProperties properties;

    private final GeneratorService generatorService;

    public AutoGenerateRunner(GeneratorProperties properties, GeneratorService generatorService)
    {
        this.properties = properties;
        this.generatorService = generatorService;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        AutoGenerate autoGenerate = properties.getAutoGenerate();
        if (autoGenerate == null || !autoGenerate.isEnabled())
        {
            return;
        }
        List<String> tables = StringUtils.splitToList(autoGenerate.getTables(), ",");
        if (tables.isEmpty())
        {
            log.warn("aidex.generator.auto-generate.enabled=true but no tables configured");
            return;
        }
        for (String tableName : tables)
        {
            GeneratorRequest request = new GeneratorRequest();
            request.setDatasource(StringUtils.defaultIfBlank(autoGenerate.getDatasource(), "sqlite"));
            request.setTableName(tableName);
            request.setPackageName(autoGenerate.getPackageName());
            request.setModuleName(autoGenerate.getModuleName());
            request.setAuthor(autoGenerate.getAuthor());
            generatorService.generate(request);
            log.info("Generated backend code for table '{}' on datasource '{}'", tableName, request.getDatasource());
        }
    }
}
