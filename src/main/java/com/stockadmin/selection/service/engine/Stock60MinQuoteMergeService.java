package com.stockadmin.selection.service.engine;

import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.domain.Stock60MinQuoteSnapshot;
import com.stockadmin.selection.domain.StockInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class Stock60MinQuoteMergeService
{
    public List<Stock60MinKlineRow> merge(StockInfo stock,
                                          List<Stock60MinKlineRow> rows,
                                          Stock60MinQuoteSnapshot snapshot)
    {
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }

        Map<Long, Stock60MinKlineRow> merged = new LinkedHashMap<Long, Stock60MinKlineRow>();
        for (Stock60MinKlineRow row : rows)
        {
            if (row == null || row.getTradeDate() == null)
            {
                continue;
            }
            merged.put(row.getTradeDate(), row);
        }

        if (stock != null && snapshot != null)
        {
            List<Stock60MinKlineRow> intradayRows = snapshot.getRowsByStock().get(stock.getCode());
            if (intradayRows != null)
            {
                for (Stock60MinKlineRow intradayRow : intradayRows)
                {
                    if (intradayRow == null || intradayRow.getTradeDate() == null)
                    {
                        continue;
                    }
                    merged.put(intradayRow.getTradeDate(), intradayRow);
                }
            }
        }

        List<Stock60MinKlineRow> mergedRows = new ArrayList<Stock60MinKlineRow>(merged.values());
        Collections.sort(mergedRows, new Comparator<Stock60MinKlineRow>()
        {
            @Override
            public int compare(Stock60MinKlineRow left, Stock60MinKlineRow right)
            {
                long leftValue = left == null || left.getTradeDate() == null ? 0L : left.getTradeDate().longValue();
                long rightValue = right == null || right.getTradeDate() == null ? 0L : right.getTradeDate().longValue();
                return leftValue < rightValue ? -1 : (leftValue == rightValue ? 0 : 1);
            }
        });
        return mergedRows;
    }
}
