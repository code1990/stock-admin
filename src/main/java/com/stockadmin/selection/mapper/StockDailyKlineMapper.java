package com.stockadmin.selection.mapper;

import com.stockadmin.selection.domain.StockDailyKlineRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockDailyKlineMapper
{
    Integer selectLatestTradeDate();

    List<StockDailyKlineRow> selectByStockCodesAndTradeDate(@Param("stockCodes") List<String> stockCodes,
                                                            @Param("tradeDate") Integer tradeDate);
}
