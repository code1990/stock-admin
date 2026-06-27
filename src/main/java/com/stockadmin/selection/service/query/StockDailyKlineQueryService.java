package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.StockDailyKlineRow;
import com.stockadmin.selection.mapper.StockDailyKlineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StockDailyKlineQueryService
{
    private static final Logger log = LoggerFactory.getLogger(StockDailyKlineQueryService.class);
    private static final int SQLITE_IN_BATCH_SIZE = 800;

    private final StockDailyKlineMapper stockDailyKlineMapper;

    public StockDailyKlineQueryService(StockDailyKlineMapper stockDailyKlineMapper)
    {
        this.stockDailyKlineMapper = stockDailyKlineMapper;
    }

    public Integer findLatestTradeDate()
    {
        try
        {
            return stockDailyKlineMapper.selectLatestTradeDate();
        }
        catch (DataAccessException ex)
        {
            log.warn("Query t_stock_daily_240 latest trade date failed, return empty. message={}", ex.getMessage());
            return null;
        }
    }

    public List<StockDailyKlineRow> queryByStockCodesAndTradeDate(List<String> stockCodes, Integer tradeDate)
    {
        if (stockCodes == null || stockCodes.isEmpty() || tradeDate == null)
        {
            return Collections.emptyList();
        }
        List<StockDailyKlineRow> rows = new ArrayList<StockDailyKlineRow>();
        for (int start = 0; start < stockCodes.size(); start += SQLITE_IN_BATCH_SIZE)
        {
            int end = Math.min(start + SQLITE_IN_BATCH_SIZE, stockCodes.size());
            try
            {
                rows.addAll(stockDailyKlineMapper.selectByStockCodesAndTradeDate(stockCodes.subList(start, end), tradeDate));
            }
            catch (DataAccessException ex)
            {
                log.warn("Query t_stock_daily_240 failed, return partial rows. start={}, end={}, message={}",
                        Integer.valueOf(start), Integer.valueOf(end), ex.getMessage());
                return rows;
            }
        }
        return rows;
    }
}
