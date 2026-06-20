package com.stockadmin.selection.service.engine;

import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.domain.StockDailyKlineRow;
import org.springframework.stereotype.Component;
import zemscript.plugin.Kline;
import zemscript.runtime.ZemNumber;

import java.util.ArrayList;
import java.util.List;

@Component
public class StockKlineBuilder
{
    public Kline build(String code, String stockName, List<StockDailyKlineRow> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return null;
        }

        Kline kline = new Kline();
        List<ZemNumber> openList = new ArrayList<ZemNumber>();
        List<ZemNumber> closeList = new ArrayList<ZemNumber>();
        List<ZemNumber> highList = new ArrayList<ZemNumber>();
        List<ZemNumber> lowList = new ArrayList<ZemNumber>();
        List<ZemNumber> percentList = new ArrayList<ZemNumber>();
        List<ZemNumber> volList = new ArrayList<ZemNumber>();
        List<ZemNumber> amountList = new ArrayList<ZemNumber>();
        List<ZemNumber> dateList = new ArrayList<ZemNumber>();
        List<ZemNumber> preCloseList = new ArrayList<ZemNumber>();

        for (StockDailyKlineRow row : rows)
        {
            openList.add(new ZemNumber(valueOrZero(row.getOpen())));
            closeList.add(new ZemNumber(valueOrZero(row.getClose())));
            highList.add(new ZemNumber(valueOrZero(row.getHigh())));
            lowList.add(new ZemNumber(valueOrZero(row.getLow())));
            percentList.add(new ZemNumber(valueOrZero(row.getPercent())));
            volList.add(new ZemNumber(valueOrZero(row.getVol())));
            amountList.add(new ZemNumber(valueOrZero(row.getAmount())));
            dateList.add(new ZemNumber(row.getTradeDate() == null ? 0.0d : row.getTradeDate().doubleValue()));
            preCloseList.add(new ZemNumber(valueOrZero(row.getPreClose())));
        }

        kline.setCode(code);
        kline.setName(stockName);
        kline.setOpen(openList);
        kline.setClose(closeList);
        kline.setHigh(highList);
        kline.setLow(lowList);
        kline.setPercent(percentList);
        kline.setVol(volList);
        kline.setAmount(amountList);
        kline.setDate(dateList);
        kline.setPreClose(preCloseList);
        kline.setSize(rows.size());
        return kline;
    }

    public Kline build60Min(String code, String stockName, List<Stock60MinKlineRow> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return null;
        }

        Kline kline = new Kline();
        List<ZemNumber> openList = new ArrayList<ZemNumber>();
        List<ZemNumber> closeList = new ArrayList<ZemNumber>();
        List<ZemNumber> highList = new ArrayList<ZemNumber>();
        List<ZemNumber> lowList = new ArrayList<ZemNumber>();
        List<ZemNumber> percentList = new ArrayList<ZemNumber>();
        List<ZemNumber> volList = new ArrayList<ZemNumber>();
        List<ZemNumber> amountList = new ArrayList<ZemNumber>();
        List<ZemNumber> dateList = new ArrayList<ZemNumber>();
        List<ZemNumber> preCloseList = new ArrayList<ZemNumber>();

        for (Stock60MinKlineRow row : rows)
        {
            openList.add(new ZemNumber(valueOrZero(row.getOpen())));
            closeList.add(new ZemNumber(valueOrZero(row.getClose())));
            highList.add(new ZemNumber(valueOrZero(row.getHigh())));
            lowList.add(new ZemNumber(valueOrZero(row.getLow())));
            percentList.add(new ZemNumber(valueOrZero(row.getPercent())));
            volList.add(new ZemNumber(valueOrZero(row.getVol())));
            amountList.add(new ZemNumber(valueOrZero(row.getAmount())));
            dateList.add(new ZemNumber(row.getTradeDate() == null ? 0.0d : row.getTradeDate().doubleValue()));
            preCloseList.add(new ZemNumber(valueOrZero(row.getPreClose())));
        }

        kline.setCode(code);
        kline.setName(stockName);
        kline.setOpen(openList);
        kline.setClose(closeList);
        kline.setHigh(highList);
        kline.setLow(lowList);
        kline.setPercent(percentList);
        kline.setVol(volList);
        kline.setAmount(amountList);
        kline.setDate(dateList);
        kline.setPreClose(preCloseList);
        kline.setSize(rows.size());
        return kline;
    }

    private double valueOrZero(Double value)
    {
        return value == null ? 0.0d : value.doubleValue();
    }
}
