package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.StockInfo;
import com.stockadmin.selection.mapper.StockPoolMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class StockPoolQueryService
{
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
        return stockPoolMapper.selectByCodes(stockCodes == null ? Collections.<String>emptyList() : stockCodes);
    }
}
