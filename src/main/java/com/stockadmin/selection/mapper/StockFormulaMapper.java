package com.stockadmin.selection.mapper;

import com.stockadmin.selection.domain.StockFormulaDefinition;
import org.apache.ibatis.annotations.Param;

public interface StockFormulaMapper
{
    StockFormulaDefinition selectByName(@Param("name") String name);
}
