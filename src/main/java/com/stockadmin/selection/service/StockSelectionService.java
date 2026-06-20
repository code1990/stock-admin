package com.stockadmin.selection.service;

import com.stockadmin.common.BusinessException;
import com.stockadmin.selection.config.SelectionProperties;
import com.stockadmin.selection.domain.FormulaEvaluationResult;
import com.stockadmin.selection.domain.RealtimeQuoteSnapshot;
import com.stockadmin.selection.domain.StockDailyKlineRow;
import com.stockadmin.selection.domain.StockFormulaDefinition;
import com.stockadmin.selection.domain.StockInfo;
import com.stockadmin.selection.dto.StockSelectionHitItem;
import com.stockadmin.selection.dto.StockSelectionRequest;
import com.stockadmin.selection.dto.StockSelectionResponse;
import com.stockadmin.selection.service.engine.StockFormulaEngineService;
import com.stockadmin.selection.service.engine.StockDailyQuoteMergeService;
import com.stockadmin.selection.service.query.StockDailyKlineQueryService;
import com.stockadmin.selection.service.query.StockFormulaQueryService;
import com.stockadmin.selection.service.query.StockPoolQueryService;
import com.stockadmin.selection.service.query.StockSelectionQuoteQueryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockSelectionService
{
    private final SelectionProperties selectionProperties;
    private final StockFormulaQueryService stockFormulaQueryService;
    private final StockPoolQueryService stockPoolQueryService;
    private final StockDailyKlineQueryService stockDailyKlineQueryService;
    private final StockSelectionQuoteQueryService stockSelectionQuoteQueryService;
    private final StockDailyQuoteMergeService stockDailyQuoteMergeService;
    private final StockFormulaEngineService stockFormulaEngineService;

    public StockSelectionService(SelectionProperties selectionProperties,
                                 StockFormulaQueryService stockFormulaQueryService,
                                 StockPoolQueryService stockPoolQueryService,
                                 StockDailyKlineQueryService stockDailyKlineQueryService,
                                 StockSelectionQuoteQueryService stockSelectionQuoteQueryService,
                                 StockDailyQuoteMergeService stockDailyQuoteMergeService,
                                 StockFormulaEngineService stockFormulaEngineService)
    {
        this.selectionProperties = selectionProperties;
        this.stockFormulaQueryService = stockFormulaQueryService;
        this.stockPoolQueryService = stockPoolQueryService;
        this.stockDailyKlineQueryService = stockDailyKlineQueryService;
        this.stockSelectionQuoteQueryService = stockSelectionQuoteQueryService;
        this.stockDailyQuoteMergeService = stockDailyQuoteMergeService;
        this.stockFormulaEngineService = stockFormulaEngineService;
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

        List<String> stockCodes = new ArrayList<String>(stocks.size());
        for (StockInfo stock : stocks)
        {
            stockCodes.add(stock.getCode());
        }

        List<StockDailyKlineRow> allRows = stockDailyKlineQueryService.queryByStockCodesAndTradeDate(stockCodes, targetTradeDate);
        Map<String, List<StockDailyKlineRow>> rowsByStock = groupRowsByStock(allRows);
        List<StockSelectionHitItem> hits = new ArrayList<StockSelectionHitItem>();

        for (StockInfo stock : stocks)
        {
            List<StockDailyKlineRow> rows = rowsByStock.get(stock.getCode());
            if (rows == null || rows.isEmpty())
            {
                continue;
            }

            List<StockDailyKlineRow> mergedRows = stockDailyQuoteMergeService.merge(stock, rows, quoteSnapshot);
            FormulaEvaluationResult evaluationResult = stockFormulaEngineService.evaluate(formula, stock, mergedRows, targetTradeDate);
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

    private Map<String, List<StockDailyKlineRow>> groupRowsByStock(List<StockDailyKlineRow> rows)
    {
        Map<String, List<StockDailyKlineRow>> rowsByStock = new HashMap<String, List<StockDailyKlineRow>>();
        for (StockDailyKlineRow row : rows)
        {
            if (row == null || !hasText(row.getStockCode()))
            {
                continue;
            }
            List<StockDailyKlineRow> stockRows = rowsByStock.get(row.getStockCode());
            if (stockRows == null)
            {
                stockRows = new ArrayList<StockDailyKlineRow>();
                rowsByStock.put(row.getStockCode(), stockRows);
            }
            stockRows.add(row);
        }
        return rowsByStock;
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
