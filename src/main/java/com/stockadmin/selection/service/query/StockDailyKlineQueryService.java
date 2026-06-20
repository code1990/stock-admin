package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.StockDailyKlineRow;
import com.stockadmin.selection.mapper.StockDailyKlineMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class StockDailyKlineQueryService
{
    private final StockDailyKlineMapper stockDailyKlineMapper;

    public StockDailyKlineQueryService(StockDailyKlineMapper stockDailyKlineMapper)
    {
        this.stockDailyKlineMapper = stockDailyKlineMapper;
    }

    public Integer findLatestTradeDate()
    {
        return stockDailyKlineMapper.selectLatestTradeDate();
    }

    public List<StockDailyKlineRow> queryByStockCodesAndTradeDate(List<String> stockCodes, Integer tradeDate)
    {
        if (stockCodes == null || stockCodes.isEmpty() || tradeDate == null)
        {
            return Collections.emptyList();
        }
        return stockDailyKlineMapper.selectByStockCodesAndTradeDate(stockCodes, tradeDate);
    }
}
