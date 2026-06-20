package com.stockadmin.selection.mapper;

import com.stockadmin.selection.domain.Stock60MinPoolEntry;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface Stock60MinPoolMapper
{
    List<Stock60MinPoolEntry> selectBySignalName(@Param("signalName") String signalName);

    List<Stock60MinPoolEntry> selectBySignalNameAndStockCodes(@Param("signalName") String signalName,
                                                              @Param("stockCodes") List<String> stockCodes);
}
