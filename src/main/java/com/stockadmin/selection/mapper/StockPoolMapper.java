package com.stockadmin.selection.mapper;

import com.stockadmin.selection.domain.StockInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockPoolMapper
{
    List<StockInfo> selectEnabledStocks();

    List<StockInfo> selectByCodes(@Param("stockCodes") List<String> stockCodes);
}
