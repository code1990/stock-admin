package com.stockadmin.selection.service.engine;

import com.stockadmin.selection.domain.RealtimeQuoteSnapshot;
import com.stockadmin.selection.domain.StockDailyKlineRow;
import com.stockadmin.selection.domain.StockInfo;
import com.stockadmin.selection.domain.StockRealtimeQuoteRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockDailyQuoteMergeService
{
    public List<StockDailyKlineRow> merge(StockInfo stock,
                                          List<StockDailyKlineRow> rows,
                                          RealtimeQuoteSnapshot snapshot)
    {
        List<StockDailyKlineRow> mergedRows = new ArrayList<StockDailyKlineRow>(rows);
        if (stock == null || mergedRows.isEmpty() || snapshot == null)
        {
            return mergedRows;
        }

        StockRealtimeQuoteRow quote = findRealtimeQuote(stock, snapshot);
        if (quote == null)
        {
            return mergedRows;
        }

        Integer quoteTradeDate = parseTradeDate(quote.getMarketDate());
        if (quoteTradeDate == null || quote.getLastPx() == null)
        {
            return mergedRows;
        }

        StockDailyKlineRow lastRow = mergedRows.get(mergedRows.size() - 1);
        Integer lastTradeDate = lastRow.getTradeDate();
        if (lastTradeDate != null && quoteTradeDate.intValue() <= lastTradeDate.intValue())
        {
            return mergedRows;
        }

        Double basePreClose = firstNonNull(quote.getPreclosePx(), lastRow.getClose(), lastRow.getPreClose());
        Double close = firstNonNull(quote.getLastPx(), lastRow.getClose());
        if (close == null)
        {
            return mergedRows;
        }

        Double open = firstNonNull(quote.getOpenPx(), close, basePreClose);
        Double high = firstNonNull(quote.getHighPx(), maxValue(open, close, basePreClose));
        Double low = firstNonNull(quote.getLowPx(), minValue(open, close, basePreClose));
        Double vol = firstNonNull(quote.getBusinessAmount(), 0.0d);
        Double amount = firstNonNull(quote.getCurrentAmount(), 0.0d);
        Double percent = firstNonNull(quote.getPxChangeRate(), calculatePercent(close, basePreClose));

        StockDailyKlineRow mergedRow = new StockDailyKlineRow();
        mergedRow.setStockCode(stock.getCode());
        mergedRow.setStockName(hasText(quote.getProdName()) ? quote.getProdName().trim() : stock.getStockName());
        mergedRow.setTradeDate(quoteTradeDate);
        mergedRow.setOpen(open);
        mergedRow.setHigh(high);
        mergedRow.setLow(low);
        mergedRow.setClose(close);
        mergedRow.setVol(vol);
        mergedRow.setAmount(amount);
        mergedRow.setPercent(percent);
        mergedRow.setPreClose(basePreClose);
        mergedRows.add(mergedRow);
        return mergedRows;
    }

    private StockRealtimeQuoteRow findRealtimeQuote(StockInfo stock, RealtimeQuoteSnapshot snapshot)
    {
        String code = safe(stock.getCode()).toUpperCase();
        if (code.isEmpty())
        {
            return null;
        }

        List<String> candidates = buildQuoteCodeCandidates(code, stock.getMarketCode());
        for (String candidate : candidates)
        {
            StockRealtimeQuoteRow quote = snapshot.getQuoteByRawCode().get(candidate);
            if (quote != null)
            {
                return quote;
            }
        }
        return snapshot.getQuoteByThsCode().get(code);
    }

    private List<String> buildQuoteCodeCandidates(String code, String marketCode)
    {
        List<String> candidates = new ArrayList<String>();
        String normalizedMarketCode = safe(marketCode);

        if ("17".equals(normalizedMarketCode))
        {
            addCandidate(candidates, code + ".SS");
            addCandidate(candidates, code + ".SH");
        }
        else if ("33".equals(normalizedMarketCode))
        {
            addCandidate(candidates, code + ".SZ");
        }
        else if (code.startsWith("6") || code.startsWith("5") || code.startsWith("9"))
        {
            addCandidate(candidates, code + ".SS");
            addCandidate(candidates, code + ".SH");
        }
        else if (code.startsWith("0") || code.startsWith("2") || code.startsWith("3"))
        {
            addCandidate(candidates, code + ".SZ");
        }
        else if (code.startsWith("4") || code.startsWith("8"))
        {
            addCandidate(candidates, code + ".BJ");
        }

        addCandidate(candidates, code);
        return candidates;
    }

    private void addCandidate(List<String> candidates, String candidate)
    {
        String value = safe(candidate).toUpperCase();
        if (!value.isEmpty() && !candidates.contains(value))
        {
            candidates.add(value);
        }
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

    private Double calculatePercent(Double close, Double preClose)
    {
        if (close == null || preClose == null || preClose.doubleValue() == 0.0d)
        {
            return null;
        }
        return (close.doubleValue() - preClose.doubleValue()) * 100.0d / preClose.doubleValue();
    }

    private Double maxValue(Double... values)
    {
        Double result = null;
        for (Double value : values)
        {
            if (value == null)
            {
                continue;
            }
            if (result == null || value.doubleValue() > result.doubleValue())
            {
                result = value;
            }
        }
        return result;
    }

    private Double minValue(Double... values)
    {
        Double result = null;
        for (Double value : values)
        {
            if (value == null)
            {
                continue;
            }
            if (result == null || value.doubleValue() < result.doubleValue())
            {
                result = value;
            }
        }
        return result;
    }

    private Double firstNonNull(Double... values)
    {
        for (Double value : values)
        {
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value)
    {
        return value != null && value.trim().length() > 0;
    }

    private String safe(String value)
    {
        return value == null ? "" : value.trim();
    }
}
