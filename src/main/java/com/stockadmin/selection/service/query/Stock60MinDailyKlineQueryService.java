package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.mapper.Stock60MinDailyKlineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class Stock60MinDailyKlineQueryService
{
    private static final Logger log = LoggerFactory.getLogger(Stock60MinDailyKlineQueryService.class);
    private static final int SQLITE_IN_BATCH_SIZE = 800;

    private final Stock60MinDailyKlineMapper stock60MinDailyKlineMapper;

    public Stock60MinDailyKlineQueryService(Stock60MinDailyKlineMapper stock60MinDailyKlineMapper)
    {
        this.stock60MinDailyKlineMapper = stock60MinDailyKlineMapper;
    }

    public Integer findLatestTradeDate()
    {
        Long latestTradeDate;
        try
        {
            latestTradeDate = stock60MinDailyKlineMapper.selectLatestTradeDate();
        }
        catch (DataAccessException ex)
        {
            log.warn("Query t_stock_daily_60 latest trade time failed, return empty. message={}", ex.getMessage());
            return null;
        }
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
        Long endTradeDateExclusive = Long.valueOf((tradeDate.intValue() + 1L) * 10000L);
        List<Stock60MinKlineRow> rows = new ArrayList<Stock60MinKlineRow>();
        for (int start = 0; start < stockCodes.size(); start += SQLITE_IN_BATCH_SIZE)
        {
            int end = Math.min(start + SQLITE_IN_BATCH_SIZE, stockCodes.size());
            try
            {
                rows.addAll(stock60MinDailyKlineMapper.selectByStockCodesAndTradeDate(stockCodes.subList(start, end), endTradeDateExclusive));
            }
            catch (DataAccessException ex)
            {
                log.warn("Query t_stock_daily_60 failed, return partial rows. start={}, end={}, message={}",
                        Integer.valueOf(start), Integer.valueOf(end), ex.getMessage());
                return rows;
            }
        }
        return rows;
    }
}
