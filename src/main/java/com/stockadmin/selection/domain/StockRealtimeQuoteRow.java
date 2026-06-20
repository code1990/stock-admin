package com.stockadmin.selection.domain;

public class StockRealtimeQuoteRow
{
    private String stockCode;
    private String prodName;
    private String marketDate;
    private Double openPx;
    private Double highPx;
    private Double lowPx;
    private Double lastPx;
    private Double preclosePx;
    private Double businessAmount;
    private Double currentAmount;
    private Double pxChangeRate;

    public String getStockCode()
    {
        return stockCode;
    }

    public void setStockCode(String stockCode)
    {
        this.stockCode = stockCode;
    }

    public String getProdName()
    {
        return prodName;
    }

    public void setProdName(String prodName)
    {
        this.prodName = prodName;
    }

    public String getMarketDate()
    {
        return marketDate;
    }

    public void setMarketDate(String marketDate)
    {
        this.marketDate = marketDate;
    }

    public Double getOpenPx()
    {
        return openPx;
    }

    public void setOpenPx(Double openPx)
    {
        this.openPx = openPx;
    }

    public Double getHighPx()
    {
        return highPx;
    }

    public void setHighPx(Double highPx)
    {
        this.highPx = highPx;
    }

    public Double getLowPx()
    {
        return lowPx;
    }

    public void setLowPx(Double lowPx)
    {
        this.lowPx = lowPx;
    }

    public Double getLastPx()
    {
        return lastPx;
    }

    public void setLastPx(Double lastPx)
    {
        this.lastPx = lastPx;
    }

    public Double getPreclosePx()
    {
        return preclosePx;
    }

    public void setPreclosePx(Double preclosePx)
    {
        this.preclosePx = preclosePx;
    }

    public Double getBusinessAmount()
    {
        return businessAmount;
    }

    public void setBusinessAmount(Double businessAmount)
    {
        this.businessAmount = businessAmount;
    }

    public Double getCurrentAmount()
    {
        return currentAmount;
    }

    public void setCurrentAmount(Double currentAmount)
    {
        this.currentAmount = currentAmount;
    }

    public Double getPxChangeRate()
    {
        return pxChangeRate;
    }

    public void setPxChangeRate(Double pxChangeRate)
    {
        this.pxChangeRate = pxChangeRate;
    }
}
