package com.stockadmin.selection.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Stock60MinQuoteSnapshot
{
    private final Map<String, List<Stock60MinKlineRow>> rowsByStock;
    private final int latestDailyTradeDate;

    public Stock60MinQuoteSnapshot(Map<String, List<Stock60MinKlineRow>> rowsByStock, int latestDailyTradeDate)
    {
        this.rowsByStock = rowsByStock == null ? Collections.<String, List<Stock60MinKlineRow>>emptyMap() : rowsByStock;
        this.latestDailyTradeDate = latestDailyTradeDate;
    }

    public Map<String, List<Stock60MinKlineRow>> getRowsByStock()
    {
        return rowsByStock;
    }

    public int getLatestDailyTradeDate()
    {
        return latestDailyTradeDate;
    }
}
