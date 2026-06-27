package com.stockadmin.selection.dto;

import java.util.List;

public class StockNmEvaluateRequest
{
    private static final String DEFAULT_NM_FORMULA_CODE =
            "NM:((4*SMA((((C-LLV(L,5))/(HHV(H,5)-LLV(L,5)))*100),5,1))"
                    + "-(3*SMA(SMA((((C-LLV(L,5))/(HHV(H,5)-LLV(L,5)))*100),5,1),3.2,1)));";

    private String formulaCode;
    private String period;
    private Integer tradeDate;
    private List<String> stockCodes;

    public String getFormulaCode()
    {
        if (formulaCode == null || formulaCode.trim().isEmpty())
        {
            return DEFAULT_NM_FORMULA_CODE;
        }
        return formulaCode;
    }

    public void setFormulaCode(String formulaCode)
    {
        this.formulaCode = formulaCode;
    }

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

    public List<String> getStockCodes()
    {
        return stockCodes;
    }

    public void setStockCodes(List<String> stockCodes)
    {
        this.stockCodes = stockCodes;
    }
}
