package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.domain.Stock60MinQuoteSnapshot;
import com.stockadmin.selection.mapper.Stock60MinQuoteMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class Stock60MinQuoteQueryService
{
    private final Stock60MinQuoteMapper stock60MinQuoteMapper;

    public Stock60MinQuoteQueryService(Stock60MinQuoteMapper stock60MinQuoteMapper)
    {
        this.stock60MinQuoteMapper = stock60MinQuoteMapper;
    }

    public Integer findLatestTradeDate()
    {
        Long latestTradeDate = stock60MinQuoteMapper.selectLatestTradeDate();
        if (latestTradeDate == null || latestTradeDate.longValue() <= 0L)
        {
            return null;
        }
        return Integer.valueOf((int) (latestTradeDate.longValue() / 10000L));
    }

    public Stock60MinQuoteSnapshot querySnapshot(List<String> stockCodes, Integer tradeDate)
    {
        if (stockCodes == null || stockCodes.isEmpty() || tradeDate == null)
        {
            return new Stock60MinQuoteSnapshot(Collections.<String, List<Stock60MinKlineRow>>emptyMap(), 0);
        }

        long startTradeDateInclusive = tradeDate.intValue() * 10000L;
        long endTradeDateExclusive = (tradeDate.intValue() + 1L) * 10000L;
        List<Stock60MinKlineRow> rows = stock60MinQuoteMapper.selectByStockCodesAndTradeDate(
                stockCodes,
                Long.valueOf(startTradeDateInclusive),
                Long.valueOf(endTradeDateExclusive)
        );

        Map<String, List<Stock60MinKlineRow>> rowsByStock = new LinkedHashMap<String, List<Stock60MinKlineRow>>();
        for (Stock60MinKlineRow row : rows)
        {
            if (row == null || !hasText(row.getStockCode()))
            {
                continue;
            }
            List<Stock60MinKlineRow> stockRows = rowsByStock.get(row.getStockCode());
            if (stockRows == null)
            {
                stockRows = new ArrayList<Stock60MinKlineRow>();
                rowsByStock.put(row.getStockCode(), stockRows);
            }
            stockRows.add(row);
        }
        return new Stock60MinQuoteSnapshot(rowsByStock, rowsByStock.isEmpty() ? 0 : tradeDate.intValue());
    }

    private boolean hasText(String value)
    {
        return value != null && value.trim().length() > 0;
    }
}
