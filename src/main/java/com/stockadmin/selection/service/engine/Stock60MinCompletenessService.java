package com.stockadmin.selection.service.engine;

import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.domain.Stock60MinSelectionContext;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class Stock60MinCompletenessService
{
    public Stock60MinSelectionContext resolveSelectionContext(Map<String, List<Stock60MinKlineRow>> rowsByStock, Integer targetTradeDate)
    {
        if (rowsByStock == null || rowsByStock.isEmpty() || targetTradeDate == null)
        {
            return new Stock60MinSelectionContext(Integer.valueOf(0), Collections.<Integer>emptySet());
        }

        int latestSlotIndex = 0;
        Set<Integer> existingSlots = new LinkedHashSet<Integer>();
        for (List<Stock60MinKlineRow> rows : rowsByStock.values())
        {
            if (rows == null)
            {
                continue;
            }
            for (Stock60MinKlineRow row : rows)
            {
                if (row == null || row.getTradeDate() == null)
                {
                    continue;
                }
                if (Stock60MinSlotSupport.toDailyTradeDate(row.getTradeDate().longValue()) != targetTradeDate.intValue())
                {
                    continue;
                }
                int slotIndex = Stock60MinSlotSupport.resolveSlotIndexByTradeDate(row.getTradeDate().longValue());
                if (slotIndex <= 0)
                {
                    continue;
                }
                existingSlots.add(Integer.valueOf(slotIndex));
                if (slotIndex > latestSlotIndex)
                {
                    latestSlotIndex = slotIndex;
                }
            }
        }
        return new Stock60MinSelectionContext(Integer.valueOf(latestSlotIndex), existingSlots);
    }

    public boolean hasCompleteSlots(List<Stock60MinKlineRow> rows, Integer targetTradeDate, Integer latestSlotIndex)
    {
        if (rows == null || rows.isEmpty() || targetTradeDate == null || latestSlotIndex == null || latestSlotIndex.intValue() <= 0)
        {
            return false;
        }

        Set<Integer> stockSlots = new LinkedHashSet<Integer>();
        for (Stock60MinKlineRow row : rows)
        {
            if (row == null || row.getTradeDate() == null)
            {
                continue;
            }
            if (Stock60MinSlotSupport.toDailyTradeDate(row.getTradeDate().longValue()) != targetTradeDate.intValue())
            {
                continue;
            }
            int slotIndex = Stock60MinSlotSupport.resolveSlotIndexByTradeDate(row.getTradeDate().longValue());
            if (slotIndex > 0)
            {
                stockSlots.add(Integer.valueOf(slotIndex));
            }
        }

        for (int slotIndex = 1; slotIndex <= latestSlotIndex.intValue(); slotIndex++)
        {
            if (!stockSlots.contains(Integer.valueOf(slotIndex)))
            {
                return false;
            }
        }
        return true;
    }
}
