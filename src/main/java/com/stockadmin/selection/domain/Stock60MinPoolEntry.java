package com.stockadmin.selection.domain;

public class Stock60MinPoolEntry
{
    private String stockCode;
    private String stockName;
    private String marketCode;
    private String signalName;
    private Integer orgNum;

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

    public String getSignalName()
    {
        return signalName;
    }

    public void setSignalName(String signalName)
    {
        this.signalName = signalName;
    }

    public Integer getOrgNum()
    {
        return orgNum;
    }

    public void setOrgNum(Integer orgNum)
    {
        this.orgNum = orgNum;
    }
}
