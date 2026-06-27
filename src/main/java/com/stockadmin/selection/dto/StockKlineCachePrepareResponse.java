package com.stockadmin.selection.dto;

public class StockKlineCachePrepareResponse
{
    private String period;
    private Integer tradeDate;
    private Integer stockCount;
    private String filePath;
    private Long fileSizeBytes;
    private Long elapsedMs;
    private Boolean includeQuote;

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

    public Integer getStockCount()
    {
        return stockCount;
    }

    public void setStockCount(Integer stockCount)
    {
        this.stockCount = stockCount;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public Long getFileSizeBytes()
    {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes)
    {
        this.fileSizeBytes = fileSizeBytes;
    }

    public Long getElapsedMs()
    {
        return elapsedMs;
    }

    public void setElapsedMs(Long elapsedMs)
    {
        this.elapsedMs = elapsedMs;
    }

    public Boolean getIncludeQuote()
    {
        return includeQuote;
    }

    public void setIncludeQuote(Boolean includeQuote)
    {
        this.includeQuote = includeQuote;
    }
}
