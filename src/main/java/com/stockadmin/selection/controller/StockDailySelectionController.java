package com.stockadmin.selection.controller;

import com.stockadmin.common.ApiResponse;
import com.stockadmin.selection.dto.StockKlineCachePrepareResponse;
import com.stockadmin.selection.dto.StockNmEvaluateRequest;
import com.stockadmin.selection.dto.StockNmEvaluateResponse;
import com.stockadmin.selection.dto.StockSelectionRequest;
import com.stockadmin.selection.dto.StockSelectionResponse;
import com.stockadmin.selection.service.SelectionPeriod;
import com.stockadmin.selection.service.Stock60MinSelectionService;
import com.stockadmin.selection.service.StockSelectionService;
import com.stockadmin.selection.service.cache.KlineBinaryCacheService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping({ "/api/selection/day", "/api/selection/240min" })
public class StockDailySelectionController
{
    private final StockSelectionService stockSelectionService;
    private final Stock60MinSelectionService stock60MinSelectionService;
    private final KlineBinaryCacheService klineBinaryCacheService;

    public StockDailySelectionController(StockSelectionService stockSelectionService,
                                         Stock60MinSelectionService stock60MinSelectionService,
                                         KlineBinaryCacheService klineBinaryCacheService)
    {
        this.stockSelectionService = stockSelectionService;
        this.stock60MinSelectionService = stock60MinSelectionService;
        this.klineBinaryCacheService = klineBinaryCacheService;
    }

    @PostMapping("/run")
    public ApiResponse<StockSelectionResponse> run(@Valid @RequestBody StockSelectionRequest request)
    {
        return ApiResponse.success(stockSelectionService.runDailySelection(request));
    }

    @PostMapping("/evaluate")
    public ApiResponse<StockNmEvaluateResponse> evaluate(@Valid @RequestBody StockNmEvaluateRequest request)
    {
        if (SelectionPeriod.isSixtyMin(request == null ? null : request.getPeriod()))
        {
            return ApiResponse.success(stock60MinSelectionService.evaluateNm(request));
        }
        return ApiResponse.success(stockSelectionService.evaluateNm(request));
    }

    @PostMapping("/kline-cache/prepare")
    public ApiResponse<StockKlineCachePrepareResponse> prepareKlineCache(@RequestParam(value = "tradeDate", required = false) Integer tradeDate,
                                                                         @RequestParam(value = "period", required = false) String period)
    {
        if (SelectionPeriod.isSixtyMin(period))
        {
            return ApiResponse.success(klineBinaryCacheService.prepareSixtyMin(tradeDate));
        }
        return ApiResponse.success(klineBinaryCacheService.prepareDaily(tradeDate));
    }
}
