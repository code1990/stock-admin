package com.stockadmin.selection.domain;

public class StockDailyKlineRow
{
    private String stockCode;
    private String stockName;
    private Integer tradeDate;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double vol;
    private Double amount;
    private Double percent;
    private Double preClose;

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

    public Integer getTradeDate()
    {
        return tradeDate;
    }

    public void setTradeDate(Integer tradeDate)
    {
        this.tradeDate = tradeDate;
    }

    public Double getOpen()
    {
        return open;
    }

    public void setOpen(Double open)
    {
        this.open = open;
    }

    public Double getHigh()
    {
        return high;
    }

    public void setHigh(Double high)
    {
        this.high = high;
    }

    public Double getLow()
    {
        return low;
    }

    public void setLow(Double low)
    {
        this.low = low;
    }

    public Double getClose()
    {
        return close;
    }

    public void setClose(Double close)
    {
        this.close = close;
    }

    public Double getVol()
    {
        return vol;
    }

    public void setVol(Double vol)
    {
        this.vol = vol;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void setAmount(Double amount)
    {
        this.amount = amount;
    }

    public Double getPercent()
    {
        return percent;
    }

    public void setPercent(Double percent)
    {
        this.percent = percent;
    }

    public Double getPreClose()
    {
        return preClose;
    }

    public void setPreClose(Double preClose)
    {
        this.preClose = preClose;
    }
}
