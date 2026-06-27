package com.stockadmin.selection.service;

import com.stockadmin.common.BusinessException;
import com.stockadmin.selection.config.SelectionProperties;
import com.stockadmin.selection.domain.FormulaEvaluationResult;
import com.stockadmin.selection.domain.RealtimeQuoteSnapshot;
import com.stockadmin.selection.domain.StockDailyKlineRow;
import com.stockadmin.selection.domain.StockFormulaDefinition;
import com.stockadmin.selection.domain.StockInfo;
import com.stockadmin.selection.dto.StockSelectionHitItem;
import com.stockadmin.selection.dto.StockNmEvaluateItem;
import com.stockadmin.selection.dto.StockNmEvaluateRequest;
import com.stockadmin.selection.dto.StockNmEvaluateResponse;
import com.stockadmin.selection.dto.StockSelectionRequest;
import com.stockadmin.selection.dto.StockSelectionResponse;
import com.stockadmin.selection.service.engine.StockFormulaEngineService;
import com.stockadmin.selection.service.cache.KlineBinaryCacheService;
import com.stockadmin.selection.service.query.StockDailyKlineQueryService;
import com.stockadmin.selection.service.query.StockFormulaQueryService;
import com.stockadmin.selection.service.query.StockPoolQueryService;
import com.stockadmin.selection.service.query.StockSelectionQuoteQueryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class StockSelectionService
{
    private final SelectionProperties selectionProperties;
    private final StockFormulaQueryService stockFormulaQueryService;
    private final StockPoolQueryService stockPoolQueryService;
    private final StockDailyKlineQueryService stockDailyKlineQueryService;
    private final StockSelectionQuoteQueryService stockSelectionQuoteQueryService;
    private final StockFormulaEngineService stockFormulaEngineService;
    private final KlineBinaryCacheService klineBinaryCacheService;

    public StockSelectionService(SelectionProperties selectionProperties,
                                 StockFormulaQueryService stockFormulaQueryService,
                                 StockPoolQueryService stockPoolQueryService,
                                 StockDailyKlineQueryService stockDailyKlineQueryService,
                                 StockSelectionQuoteQueryService stockSelectionQuoteQueryService,
                                 StockFormulaEngineService stockFormulaEngineService,
                                 KlineBinaryCacheService klineBinaryCacheService)
    {
        this.selectionProperties = selectionProperties;
        this.stockFormulaQueryService = stockFormulaQueryService;
        this.stockPoolQueryService = stockPoolQueryService;
        this.stockDailyKlineQueryService = stockDailyKlineQueryService;
        this.stockSelectionQuoteQueryService = stockSelectionQuoteQueryService;
        this.stockFormulaEngineService = stockFormulaEngineService;
        this.klineBinaryCacheService = klineBinaryCacheService;
    }

    public StockSelectionResponse runDailySelection(StockSelectionRequest request)
    {
        validateRequest(request);

        StockFormulaDefinition formula = stockFormulaQueryService.resolveFormula(
                request.getStrategyName(),
                request.getFormulaCode()
        );

        RealtimeQuoteSnapshot quoteSnapshot = stockSelectionQuoteQueryService.queryRealtimeQuoteSnapshot();
        Integer latestDailyTradeDate = stockDailyKlineQueryService.findLatestTradeDate();
        Integer targetTradeDate = resolveTargetTradeDate(request.getTradeDate(), latestDailyTradeDate, quoteSnapshot);
        List<StockInfo> stocks = stockPoolQueryService.queryStocks(request.getStockCodes());

        if (stocks.isEmpty())
        {
            return buildResponse(formula.getName(), targetTradeDate, Collections.<StockSelectionHitItem>emptyList(), request.getLimit());
        }

        List<StockSelectionHitItem> hits = new ArrayList<StockSelectionHitItem>();
        for (StockInfo stock : stocks)
        {
            List<StockDailyKlineRow> rows = klineBinaryCacheService.loadDailyRows(stock.getCode(), targetTradeDate);
            if (rows == null || rows.isEmpty())
            {
                continue;
            }

            FormulaEvaluationResult evaluationResult = stockFormulaEngineService.evaluate(formula, stock, rows, targetTradeDate);
            if (evaluationResult == null)
            {
                continue;
            }
            hits.add(toHitItem(stock, evaluationResult));
        }

        Collections.sort(hits, new Comparator<StockSelectionHitItem>()
        {
            @Override
            public int compare(StockSelectionHitItem left, StockSelectionHitItem right)
            {
                return safe(left.getStockCode()).compareTo(safe(right.getStockCode()));
            }
        });

        return buildResponse(formula.getName(), targetTradeDate, hits, request.getLimit());
    }

    public StockNmEvaluateResponse evaluateNm(StockNmEvaluateRequest request)
    {
        if (request == null)
        {
            throw new BusinessException("request body is required");
        }
        RealtimeQuoteSnapshot quoteSnapshot = stockSelectionQuoteQueryService.queryRealtimeQuoteSnapshot();
        Integer latestDailyTradeDate = stockDailyKlineQueryService.findLatestTradeDate();
        Integer targetTradeDate = resolveTargetTradeDate(request.getTradeDate(), latestDailyTradeDate, quoteSnapshot);
        List<StockInfo> stocks = stockPoolQueryService.queryStocks(request.getStockCodes());

        List<StockNmEvaluateItem> items = new ArrayList<StockNmEvaluateItem>();
        for (StockInfo stock : stocks)
        {
            List<StockDailyKlineRow> rows = klineBinaryCacheService.loadDailyRows(stock.getCode(), targetTradeDate);
            if (rows == null || rows.isEmpty())
            {
                continue;
            }
            Double nm = stockFormulaEngineService.evaluateDailyVariable(request.getFormulaCode(), "NM", stock, rows, targetTradeDate);
            if (nm == null)
            {
                continue;
            }
            StockNmEvaluateItem item = new StockNmEvaluateItem();
            item.setStockCode(stock.getCode());
            item.setStockName(stock.getStockName());
            item.setMarketCode(stock.getMarketCode());
            item.setTradeDate(targetTradeDate);
            item.setNm(BigDecimal.valueOf(nm.doubleValue()));
            item.setPrice(resolveLatestClose(rows));
            items.add(item);
        }
        StockNmEvaluateResponse response = new StockNmEvaluateResponse();
        response.setPeriod(SelectionPeriod.PERIOD_240);
        response.setTradeDate(targetTradeDate);
        response.setTotal(Integer.valueOf(items.size()));
        response.setItems(items);
        return response;
    }

    private void validateRequest(StockSelectionRequest request)
    {
        if (request == null)
        {
            throw new BusinessException("request body is required");
        }
        boolean hasStrategyName = hasText(request.getStrategyName());
        boolean hasFormulaCode = hasText(request.getFormulaCode());
        if (hasStrategyName == hasFormulaCode)
        {
            throw new BusinessException("strategyName and formulaCode must provide exactly one");
        }
    }

    private Integer resolveTargetTradeDate(Integer requestedTradeDate,
                                           Integer latestDailyTradeDate,
                                           RealtimeQuoteSnapshot quoteSnapshot)
    {
        if (requestedTradeDate != null)
        {
            return requestedTradeDate;
        }
        int latestQuoteTradeDate = quoteSnapshot == null ? 0 : quoteSnapshot.getLatestTradeDate();
        int latestDaily = latestDailyTradeDate == null ? 0 : latestDailyTradeDate;
        int resolved = Math.max(latestDaily, latestQuoteTradeDate);
        if (resolved <= 0)
        {
            throw new BusinessException("no available trade date found from t_stock_daily_240 or t_stock_quote");
        }
        return resolved;
    }

    private BigDecimal resolveLatestClose(List<StockDailyKlineRow> rows)
    {
        for (int i = rows.size() - 1; i >= 0; i--)
        {
            StockDailyKlineRow row = rows.get(i);
            if (row != null && row.getClose() != null)
            {
                return BigDecimal.valueOf(row.getClose().doubleValue());
            }
        }
        return null;
    }

    private StockSelectionHitItem toHitItem(StockInfo stock, FormulaEvaluationResult evaluationResult)
    {
        StockSelectionHitItem item = new StockSelectionHitItem();
        item.setStockCode(stock.getCode());
        item.setStockName(stock.getStockName());
        item.setMarketCode(stock.getMarketCode());
        item.setTradeDate(evaluationResult.getTradeDate());
        item.setHitPrice(evaluationResult.getHitPrice() == null ? null : BigDecimal.valueOf(evaluationResult.getHitPrice()));
        item.setSlotTradeDate(evaluationResult.getSlotTradeDate());
        item.setSlotIndex(evaluationResult.getSlotIndex());
        return item;
    }

    private StockSelectionResponse buildResponse(String strategyName,
                                                 Integer tradeDate,
                                                 List<StockSelectionHitItem> hits,
                                                 Integer requestedLimit)
    {
        int finalLimit = requestedLimit == null ? selectionProperties.getDefaultLimit() : requestedLimit.intValue();
        if (finalLimit <= 0)
        {
            finalLimit = selectionProperties.getDefaultLimit();
        }
        List<StockSelectionHitItem> limitedHits = hits;
        if (hits.size() > finalLimit)
        {
            limitedHits = new ArrayList<StockSelectionHitItem>(hits.subList(0, finalLimit));
        }

        StockSelectionResponse response = new StockSelectionResponse();
        response.setStrategyName(strategyName);
        response.setTradeDate(tradeDate);
        response.setTotal(hits.size());
        response.setItems(limitedHits);
        return response;
    }

    private boolean hasText(String value)
    {
        return value != null && value.trim().length() > 0;
    }

    private String safe(String value)
    {
        return value == null ? "" : value;
    }
}
