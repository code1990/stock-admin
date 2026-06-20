package com.stockadmin.selection.domain;

public class FormulaEvaluationResult
{
    private final Integer tradeDate;
    private final Double hitPrice;
    private final Long slotTradeDate;
    private final Integer slotIndex;

    public FormulaEvaluationResult(Integer tradeDate, Double hitPrice)
    {
        this(tradeDate, hitPrice, null, null);
    }

    public FormulaEvaluationResult(Integer tradeDate, Double hitPrice, Long slotTradeDate, Integer slotIndex)
    {
        this.tradeDate = tradeDate;
        this.hitPrice = hitPrice;
        this.slotTradeDate = slotTradeDate;
        this.slotIndex = slotIndex;
    }

    public Integer getTradeDate()
    {
        return tradeDate;
    }

    public Double getHitPrice()
    {
        return hitPrice;
    }

    public Long getSlotTradeDate()
    {
        return slotTradeDate;
    }

    public Integer getSlotIndex()
    {
        return slotIndex;
    }
}
