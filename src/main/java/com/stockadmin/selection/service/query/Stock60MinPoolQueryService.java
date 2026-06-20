package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.Stock60MinPoolEntry;
import com.stockadmin.selection.domain.StockInfo;
import com.stockadmin.selection.mapper.Stock60MinPoolMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class Stock60MinPoolQueryService
{
    private final Stock60MinPoolMapper stock60MinPoolMapper;

    public Stock60MinPoolQueryService(Stock60MinPoolMapper stock60MinPoolMapper)
    {
        this.stock60MinPoolMapper = stock60MinPoolMapper;
    }

    public List<Stock60MinPoolEntry> queryPoolEntries(String signalName, List<String> stockCodes)
    {
        if (stockCodes == null || stockCodes.isEmpty())
        {
            return stock60MinPoolMapper.selectBySignalName(signalName);
        }
        return stock60MinPoolMapper.selectBySignalNameAndStockCodes(signalName, stockCodes);
    }

    public List<StockInfo> toStockInfos(List<Stock60MinPoolEntry> poolEntries)
    {
        Map<String, StockInfo> stockByCode = new LinkedHashMap<String, StockInfo>();
        for (Stock60MinPoolEntry entry : poolEntries)
        {
            if (entry == null || !hasText(entry.getStockCode()))
            {
                continue;
            }
            if (stockByCode.containsKey(entry.getStockCode()))
            {
                continue;
            }
            StockInfo stockInfo = new StockInfo();
            stockInfo.setCode(entry.getStockCode());
            stockInfo.setStockName(entry.getStockName());
            stockInfo.setMarketCode(entry.getMarketCode());
            stockByCode.put(entry.getStockCode(), stockInfo);
        }
        return new ArrayList<StockInfo>(stockByCode.values());
    }

    private boolean hasText(String value)
    {
        return value != null && value.trim().length() > 0;
    }
}
