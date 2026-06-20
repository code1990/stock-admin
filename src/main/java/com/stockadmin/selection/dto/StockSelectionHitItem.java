package com.stockadmin.selection.dto;

import java.math.BigDecimal;

public class StockSelectionHitItem
{
    private String stockCode;
    private String stockName;
    private String marketCode;
    private Integer tradeDate;
    private BigDecimal hitPrice;
    private Long slotTradeDate;
    private Integer slotIndex;

    public String getStockCode()
    {
        return stockCode;
    }

    public void setStockCode(String stockCode)
    {
        this.stockCode = stockCode;
    }

    public String getStockName()
    {
        return stockName;
    }

    public void setStockName(String stockName)
    {
        this.stockName = stockName;
    }

    public String getMarketCode()
    {
        return marketCode;
    }

    public void setMarketCode(String marketCode)
    {
        this.marketCode = marketCode;
    }

    public Integer getTradeDate()
    {
        return tradeDate;
    }

    public void setTradeDate(Integer tradeDate)
    {
        this.tradeDate = tradeDate;
    }

    public BigDecimal getHitPrice()
    {
        return hitPrice;
    }

    public void setHitPrice(BigDecimal hitPrice)
    {
        this.hitPrice = hitPrice;
    }

    public Long getSlotTradeDate()
    {
        return slotTradeDate;
    }

    public void setSlotTradeDate(Long slotTradeDate)
    {
        this.slotTradeDate = slotTradeDate;
    }

    public Integer getSlotIndex()
    {
        return slotIndex;
    }

    public void setSlotIndex(Integer slotIndex)
    {
        this.slotIndex = slotIndex;
    }
}
