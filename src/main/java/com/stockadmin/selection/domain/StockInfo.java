package com.stockadmin.selection.domain;

public class StockInfo
{
    private String code;
    private String stockName;
    private String marketCode;
    private String stockType;
    private Integer enabled;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
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

    public String getStockType()
    {
        return stockType;
    }

    public void setStockType(String stockType)
    {
        this.stockType = stockType;
    }

    public Integer getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Integer enabled)
    {
        this.enabled = enabled;
    }
}
