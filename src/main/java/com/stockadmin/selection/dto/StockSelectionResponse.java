package com.stockadmin.selection.dto;

import java.util.List;

public class StockSelectionResponse
{
    private String strategyName;
    private Integer tradeDate;
    private Integer total;
    private List<StockSelectionHitItem> items;

    public String getStrategyName()
    {
        return strategyName;
    }

    public void setStrategyName(String strategyName)
    {
        this.strategyName = strategyName;
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

    public List<StockSelectionHitItem> getItems()
    {
        return items;
    }

    public void setItems(List<StockSelectionHitItem> items)
    {
        this.items = items;
    }
}
