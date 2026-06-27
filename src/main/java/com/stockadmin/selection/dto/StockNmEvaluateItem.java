package com.stockadmin.selection.dto;

import java.math.BigDecimal;

public class StockNmEvaluateItem
{
    private String stockCode;
    private String stockName;
    private String marketCode;
    private Integer tradeDate;
    private BigDecimal price;
    private BigDecimal nm;

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

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getNm()
    {
        return nm;
    }

    public void setNm(BigDecimal nm)
    {
        this.nm = nm;
    }
}
