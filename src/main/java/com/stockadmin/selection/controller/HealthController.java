package com.stockadmin.selection.controller;

import com.stockadmin.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/health")
public class HealthController
{
    @GetMapping
    public ApiResponse<Object> health()
    {
        return ApiResponse.success(Collections.singletonMap("status", "UP"));
    }
}
