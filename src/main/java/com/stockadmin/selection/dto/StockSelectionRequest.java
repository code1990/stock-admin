package com.stockadmin.selection.dto;

import javax.validation.constraints.Min;
import java.util.List;

public class StockSelectionRequest
{
    private String strategyName;
    private String formulaCode;
    private Integer tradeDate;
    private List<String> stockCodes;

    @Min(1)
    private Integer limit;

    public String getStrategyName()
    {
        return strategyName;
    }

    public void setStrategyName(String strategyName)
    {
        this.strategyName = strategyName;
    }

    public String getFormulaCode()
    {
        return formulaCode;
    }

    public void setFormulaCode(String formulaCode)
    {
        this.formulaCode = formulaCode;
    }

    public Integer getTradeDate()
    {
        return tradeDate;
    }

    public void setTradeDate(Integer tradeDate)
    {
        this.tradeDate = tradeDate;
    }

    public List<String> getStockCodes()
    {
        return stockCodes;
    }

    public void setStockCodes(List<String> stockCodes)
    {
        this.stockCodes = stockCodes;
    }

    public Integer getLimit()
    {
        return limit;
    }

    public void setLimit(Integer limit)
    {
        this.limit = limit;
    }
}
