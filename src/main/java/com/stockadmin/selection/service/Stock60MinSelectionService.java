package com.stockadmin.selection.service;

import com.stockadmin.common.BusinessException;
import com.stockadmin.selection.config.SelectionProperties;
import com.stockadmin.selection.domain.FormulaEvaluationResult;
import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.domain.Stock60MinPoolEntry;
import com.stockadmin.selection.domain.Stock60MinSelectionContext;
import com.stockadmin.selection.domain.StockFormulaDefinition;
import com.stockadmin.selection.domain.StockInfo;
import com.stockadmin.selection.dto.StockKlineCachePrepareResponse;
import com.stockadmin.selection.dto.StockSelectionHitItem;
import com.stockadmin.selection.dto.StockSelectionRequest;
import com.stockadmin.selection.dto.StockSelectionResponse;
import com.stockadmin.selection.service.cache.KlineBinaryCacheService;
import com.stockadmin.selection.service.engine.Stock60MinCompletenessService;
import com.stockadmin.selection.service.engine.StockFormulaEngineService;
import com.stockadmin.selection.service.query.Stock60MinDailyKlineQueryService;
import com.stockadmin.selection.service.query.Stock60MinPoolQueryService;
import com.stockadmin.selection.service.query.Stock60MinQuoteQueryService;
import com.stockadmin.selection.service.query.StockFormulaQueryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Stock60MinSelectionService
{
    private final SelectionProperties selectionProperties;
    private final StockFormulaQueryService stockFormulaQueryService;
    private final Stock60MinPoolQueryService stock60MinPoolQueryService;
    private final Stock60MinDailyKlineQueryService stock60MinDailyKlineQueryService;
    private final Stock60MinQuoteQueryService stock60MinQuoteQueryService;
    private final Stock60MinCompletenessService stock60MinCompletenessService;
    private final StockFormulaEngineService stockFormulaEngineService;
    private final KlineBinaryCacheService klineBinaryCacheService;

    public Stock60MinSelectionService(SelectionProperties selectionProperties,
                                      StockFormulaQueryService stockFormulaQueryService,
                                      Stock60MinPoolQueryService stock60MinPoolQueryService,
                                      Stock60MinDailyKlineQueryService stock60MinDailyKlineQueryService,
                                      Stock60MinQuoteQueryService stock60MinQuoteQueryService,
                                      Stock60MinCompletenessService stock60MinCompletenessService,
                                      StockFormulaEngineService stockFormulaEngineService,
                                      KlineBinaryCacheService klineBinaryCacheService)
    {
        this.selectionProperties = selectionProperties;
        this.stockFormulaQueryService = stockFormulaQueryService;
        this.stock60MinPoolQueryService = stock60MinPoolQueryService;
        this.stock60MinDailyKlineQueryService = stock60MinDailyKlineQueryService;
        this.stock60MinQuoteQueryService = stock60MinQuoteQueryService;
        this.stock60MinCompletenessService = stock60MinCompletenessService;
        this.stockFormulaEngineService = stockFormulaEngineService;
        this.klineBinaryCacheService = klineBinaryCacheService;
    }

    public StockSelectionResponse runSelection(StockSelectionRequest request)
    {
        validateRequest(request);

        StockFormulaDefinition formula = stockFormulaQueryService.resolveFormula(
                request.getStrategyName(),
                request.getFormulaCode()
        );

        List<Stock60MinPoolEntry> poolEntries = stock60MinPoolQueryService.queryPoolEntries(formula.getName(), request.getStockCodes());
        List<StockInfo> stocks = stock60MinPoolQueryService.toStockInfos(poolEntries);
        Integer latestDailyTradeDate = stock60MinDailyKlineQueryService.findLatestTradeDate();
        Integer latestQuoteTradeDate = stock60MinQuoteQueryService.findLatestTradeDate();
        Integer targetTradeDate = resolveTargetTradeDate(request.getTradeDate(), latestDailyTradeDate, latestQuoteTradeDate);

        if (stocks.isEmpty())
        {
            return buildResponse(formula.getName(), targetTradeDate, Collections.<StockSelectionHitItem>emptyList(), request.getLimit());
        }

        ensureSixtyMinCache(targetTradeDate);
        Map<String, List<Stock60MinKlineRow>> mergedRowsByStock = new HashMap<String, List<Stock60MinKlineRow>>();
        for (StockInfo stock : stocks)
        {
            List<Stock60MinKlineRow> rows = klineBinaryCacheService.readSixtyMinRows(stock.getCode());
            if (rows == null || rows.isEmpty())
            {
                continue;
            }
            if (!rows.isEmpty())
            {
                mergedRowsByStock.put(stock.getCode(), rows);
            }
        }

        Stock60MinSelectionContext selectionContext = stock60MinCompletenessService.resolveSelectionContext(mergedRowsByStock, targetTradeDate);
        List<StockSelectionHitItem> hits = new ArrayList<StockSelectionHitItem>();
        for (StockInfo stock : stocks)
        {
            List<Stock60MinKlineRow> rows = mergedRowsByStock.get(stock.getCode());
            if (!stock60MinCompletenessService.hasCompleteSlots(rows, targetTradeDate, selectionContext.getLatestSlotIndex()))
            {
                continue;
            }

            FormulaEvaluationResult evaluationResult = stockFormulaEngineService.evaluate60Min(formula, stock, rows, targetTradeDate);
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

    public StockKlineCachePrepareResponse prepareSixtyMinCache(Integer tradeDate)
    {
        return klineBinaryCacheService.prepareSixtyMin(tradeDate);
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

    private Integer resolveTargetTradeDate(Integer requestedTradeDate, Integer latestDailyTradeDate, Integer latestQuoteTradeDate)
    {
        if (requestedTradeDate != null)
        {
            return requestedTradeDate;
        }
        int latestDaily = latestDailyTradeDate == null ? 0 : latestDailyTradeDate.intValue();
        int latestQuote = latestQuoteTradeDate == null ? 0 : latestQuoteTradeDate.intValue();
        int resolved = Math.max(latestDaily, latestQuote);
        if (resolved <= 0)
        {
            throw new BusinessException("no available trade date found from t_stock_daily_60 or t_stock_quote_60");
        }
        return Integer.valueOf(resolved);
    }

    private void ensureSixtyMinCache(Integer tradeDate)
    {
        if (!klineBinaryCacheService.sixtyMinCacheExists())
        {
            klineBinaryCacheService.prepareSixtyMin(tradeDate);
        }
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
        response.setTotal(Integer.valueOf(hits.size()));
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
