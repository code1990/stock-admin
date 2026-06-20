package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.mapper.Stock60MinDailyKlineMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class Stock60MinDailyKlineQueryService
{
    private final Stock60MinDailyKlineMapper stock60MinDailyKlineMapper;

    public Stock60MinDailyKlineQueryService(Stock60MinDailyKlineMapper stock60MinDailyKlineMapper)
    {
        this.stock60MinDailyKlineMapper = stock60MinDailyKlineMapper;
    }

    public Integer findLatestTradeDate()
    {
        Long latestTradeDate = stock60MinDailyKlineMapper.selectLatestTradeDate();
        if (latestTradeDate == null || latestTradeDate.longValue() <= 0L)
        {
            return null;
        }
        return Integer.valueOf((int) (latestTradeDate.longValue() / 10000L));
    }

    public List<Stock60MinKlineRow> queryByStockCodesAndTradeDate(List<String> stockCodes, Integer tradeDate)
    {
        if (stockCodes == null || stockCodes.isEmpty() || tradeDate == null)
        {
            return Collections.emptyList();
        }
        return stock60MinDailyKlineMapper.selectByStockCodesAndTradeDate(stockCodes, Long.valueOf((tradeDate.intValue() + 1L) * 10000L));
    }
}
