package com.stockadmin.selection.domain;

import java.util.Collections;
import java.util.Map;

public class RealtimeQuoteSnapshot
{
    private final Map<String, StockRealtimeQuoteRow> quoteByRawCode;
    private final Map<String, StockRealtimeQuoteRow> quoteByThsCode;
    private final int latestTradeDate;

    public RealtimeQuoteSnapshot(Map<String, StockRealtimeQuoteRow> quoteByRawCode,
                                 Map<String, StockRealtimeQuoteRow> quoteByThsCode,
                                 int latestTradeDate)
    {
        this.quoteByRawCode = quoteByRawCode == null ? Collections.<String, StockRealtimeQuoteRow>emptyMap() : quoteByRawCode;
        this.quoteByThsCode = quoteByThsCode == null ? Collections.<String, StockRealtimeQuoteRow>emptyMap() : quoteByThsCode;
        this.latestTradeDate = latestTradeDate;
    }

    public Map<String, StockRealtimeQuoteRow> getQuoteByRawCode()
    {
        return quoteByRawCode;
    }

    public Map<String, StockRealtimeQuoteRow> getQuoteByThsCode()
    {
        return quoteByThsCode;
    }

    public int getLatestTradeDate()
    {
        return latestTradeDate;
    }
}
