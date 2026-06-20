package com.aidex.web.controller;

import com.aidex.common.core.controller.BaseController;
import com.aidex.common.core.domain.AjaxResult;
import com.aidex.generator.domain.GeneratorRequest;
import com.aidex.generator.service.GeneratorService;
import com.aidex.generator.service.SchemaQueryService;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generator")
public class GeneratorController extends BaseController
{
    private final SchemaQueryService schemaQueryService;

    private final GeneratorService generatorService;

    public GeneratorController(SchemaQueryService schemaQueryService, GeneratorService generatorService)
    {
        this.schemaQueryService = schemaQueryService;
        this.generatorService = generatorService;
    }

    @GetMapping("/datasources")
    public AjaxResult datasources()
    {
        return success(schemaQueryService.listDataSources());
    }

    @GetMapping("/tables/{datasource}")
    public AjaxResult tables(@PathVariable String datasource)
    {
        return success(schemaQueryService.listTables(datasource));
    }

    @GetMapping("/tables/{datasource}/{tableName}")
    public AjaxResult table(@PathVariable String datasource, @PathVariable String tableName)
    {
        return success(schemaQueryService.loadTable(datasource, tableName));
    }

    @PostMapping("/preview")
    public AjaxResult preview(@Valid @RequestBody GeneratorRequest request)
    {
        return success(generatorService.preview(request));
    }

    @PostMapping("/download")
    public void download(@Valid @RequestBody GeneratorRequest request, HttpServletResponse response) throws Exception
    {
        byte[] zip = generatorService.generateZip(request);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + request.getTableName() + "-backend.zip\"");
        response.getOutputStream().write(zip);
        response.flushBuffer();
    }
}
