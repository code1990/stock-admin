package com.stockadmin.selection.mapper;

import com.stockadmin.selection.domain.Stock60MinKlineRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface Stock60MinDailyKlineMapper
{
    Long selectLatestTradeDate();

    List<Stock60MinKlineRow> selectByStockCodesAndTradeDate(@Param("stockCodes") List<String> stockCodes,
                                                            @Param("endTradeDateExclusive") Long endTradeDateExclusive);
}
