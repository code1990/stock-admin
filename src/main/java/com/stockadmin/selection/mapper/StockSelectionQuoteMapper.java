package com.stockadmin.selection.mapper;

import com.stockadmin.selection.domain.StockRealtimeQuoteRow;

import java.util.List;

public interface StockSelectionQuoteMapper
{
    List<StockRealtimeQuoteRow> selectAllRealtimeQuotes();
}
