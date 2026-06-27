package com.stockadmin.selection.controller;

import com.stockadmin.common.ApiResponse;
import com.stockadmin.selection.dto.StockKlineCachePrepareResponse;
import com.stockadmin.selection.dto.StockNmEvaluateRequest;
import com.stockadmin.selection.dto.StockNmEvaluateResponse;
import com.stockadmin.selection.dto.StockSelectionRequest;
import com.stockadmin.selection.dto.StockSelectionResponse;
import com.stockadmin.selection.service.Stock60MinSelectionService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/selection/60min")
public class Stock60MinSelectionController
{
    private final Stock60MinSelectionService stock60MinSelectionService;

    public Stock60MinSelectionController(Stock60MinSelectionService stock60MinSelectionService)
    {
        this.stock60MinSelectionService = stock60MinSelectionService;
    }

    @PostMapping("/run")
    public ApiResponse<StockSelectionResponse> run(@Valid @RequestBody StockSelectionRequest request)
    {
        return ApiResponse.success(stock60MinSelectionService.runSelection(request));
    }

    @PostMapping("/evaluate")
    public ApiResponse<StockNmEvaluateResponse> evaluate(@Valid @RequestBody StockNmEvaluateRequest request)
    {
        return ApiResponse.success(stock60MinSelectionService.evaluateNm(request));
    }

    @PostMapping("/kline-cache/prepare")
    public ApiResponse<StockKlineCachePrepareResponse> prepareKlineCache(@RequestParam(value = "tradeDate", required = false) Integer tradeDate)
    {
        return ApiResponse.success(stock60MinSelectionService.prepareSixtyMinCache(tradeDate));
    }
}
