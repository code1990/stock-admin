package com.aidex.web.controller;

import com.aidex.common.core.controller.BaseController;
import com.aidex.common.core.domain.AjaxResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController extends BaseController
{
    @GetMapping
    public AjaxResult health()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now().toString());
        return success(data);
    }

    @GetMapping("/about")
    public AjaxResult about()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", "aidex-admin");
        data.put("mode", "single-module backend generator");
        data.put("features", new String[] { "spring-boot", "mybatis", "multi-datasource", "mysql", "sqlite", "backend-generator" });
        return success(data);
    }
}
