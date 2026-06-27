package com.stockadmin.selection.dto;

import java.util.List;

public class StockNmEvaluateResponse
{
    private String period;
    private Integer tradeDate;
    private Integer total;
    private List<StockNmEvaluateItem> items;

    public String getPeriod()
    {
        return period;
    }

    public void setPeriod(String period)
    {
        this.period = period;
    }

    public Integer getTradeDate()
    {
        return tradeDate;
    }

    public void setTradeDate(Integer tradeDate)
    {
        this.tradeDate = tradeDate;
    }

    public Integer getTotal()
    {
        return total;
    }

    public void setTotal(Integer total)
    {
        this.total = total;
    }

    public List<StockNmEvaluateItem> getItems()
    {
        return items;
    }

    public void setItems(List<StockNmEvaluateItem> items)
    {
        this.items = items;
    }
}
