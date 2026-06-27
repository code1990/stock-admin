package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.StockInfo;
import com.stockadmin.selection.mapper.StockPoolMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StockPoolQueryService
{
    private static final int SQLITE_IN_BATCH_SIZE = 800;

    private final StockPoolMapper stockPoolMapper;

    public StockPoolQueryService(StockPoolMapper stockPoolMapper)
    {
        this.stockPoolMapper = stockPoolMapper;
    }

    public List<StockInfo> queryStocks(List<String> stockCodes)
    {
        if (stockCodes == null || stockCodes.isEmpty())
        {
            return stockPoolMapper.selectEnabledStocks();
        }
        List<StockInfo> stocks = new ArrayList<StockInfo>();
        for (int start = 0; start < stockCodes.size(); start += SQLITE_IN_BATCH_SIZE)
        {
            int end = Math.min(start + SQLITE_IN_BATCH_SIZE, stockCodes.size());
            stocks.addAll(stockPoolMapper.selectByCodes(stockCodes.subList(start, end)));
        }
        return stocks;
    }
}
