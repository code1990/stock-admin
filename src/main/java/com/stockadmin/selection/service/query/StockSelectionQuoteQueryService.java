package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.RealtimeQuoteSnapshot;
import com.stockadmin.selection.domain.StockRealtimeQuoteRow;
import com.stockadmin.selection.mapper.StockSelectionQuoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockSelectionQuoteQueryService
{
    private static final Logger log = LoggerFactory.getLogger(StockSelectionQuoteQueryService.class);

    private final StockSelectionQuoteMapper stockSelectionQuoteMapper;

    public StockSelectionQuoteQueryService(StockSelectionQuoteMapper stockSelectionQuoteMapper)
    {
        this.stockSelectionQuoteMapper = stockSelectionQuoteMapper;
    }

    public RealtimeQuoteSnapshot queryRealtimeQuoteSnapshot()
    {
        List<StockRealtimeQuoteRow> quotes;
        try
        {
            quotes = stockSelectionQuoteMapper.selectAllRealtimeQuotes();
        }
        catch (DataAccessException ex)
        {
            log.warn("Query t_stock_quote failed, continue without intraday daily quote. message={}", ex.getMessage());
            quotes = Collections.emptyList();
        }
        Map<String, StockRealtimeQuoteRow> quoteByRawCode = new HashMap<String, StockRealtimeQuoteRow>();
        Map<String, StockRealtimeQuoteRow> quoteByThsCode = new HashMap<String, StockRealtimeQuoteRow>();
        int latestTradeDate = 0;

        for (StockRealtimeQuoteRow quote : quotes)
        {
            String rawCode = safe(quote.getStockCode()).toUpperCase();
            String thsCode = normalizeThsCode(rawCode);
            if (thsCode.isEmpty())
            {
                continue;
            }
            quoteByRawCode.put(rawCode, quote);

            StockRealtimeQuoteRow current = quoteByThsCode.get(thsCode);
            if (shouldReplace(current, quote))
            {
                quoteByThsCode.put(thsCode, quote);
            }

            Integer tradeDate = parseTradeDate(quote.getMarketDate());
            if (tradeDate != null && tradeDate.intValue() > latestTradeDate && quote.getLastPx() != null)
            {
                latestTradeDate = tradeDate.intValue();
            }
        }
        return new RealtimeQuoteSnapshot(quoteByRawCode, quoteByThsCode, latestTradeDate);
    }

    private boolean shouldReplace(StockRealtimeQuoteRow current, StockRealtimeQuoteRow candidate)
    {
        if (current == null)
        {
            return true;
        }
        Integer currentDate = parseTradeDate(current.getMarketDate());
        Integer candidateDate = parseTradeDate(candidate.getMarketDate());
        if (candidateDate == null)
        {
            return false;
        }
        if (currentDate == null || candidateDate.intValue() > currentDate.intValue())
        {
            return true;
        }
        return candidateDate.intValue() == currentDate.intValue() && candidate.getLastPx() != null && current.getLastPx() == null;
    }

    private String normalizeThsCode(String rawCode)
    {
        String code = safe(rawCode).toUpperCase();
        if (code.endsWith(".SS") || code.endsWith(".SH") || code.endsWith(".SZ") || code.endsWith(".BJ"))
        {
            return code.substring(0, code.indexOf('.'));
        }
        return code;
    }

    private Integer parseTradeDate(String marketDate)
    {
        String digits = safe(marketDate).replaceAll("[^0-9]", "");
        if (digits.length() < 8)
        {
            return null;
        }
        return Integer.valueOf(digits.substring(0, 8));
    }

    private String safe(String value)
    {
        return value == null ? "" : value.trim();
    }
}
