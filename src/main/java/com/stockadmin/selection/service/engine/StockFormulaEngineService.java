package com.stockadmin.selection.service.engine;

import com.stockadmin.common.BusinessException;
import com.stockadmin.selection.domain.FormulaEvaluationResult;
import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.domain.StockDailyKlineRow;
import com.stockadmin.selection.domain.StockFormulaDefinition;
import com.stockadmin.selection.domain.StockInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zemscript.lang.Interpreter;
import zemscript.plugin.Kline;
import zemscript.plugin.util.ZemNumberArrayUtil;
import zemscript.runtime.ZemNumberArray;

import java.util.List;

@Service
public class StockFormulaEngineService
{
    private static final Logger log = LoggerFactory.getLogger(StockFormulaEngineService.class);

    private final StockKlineBuilder stockKlineBuilder;

    public StockFormulaEngineService(StockKlineBuilder stockKlineBuilder)
    {
        this.stockKlineBuilder = stockKlineBuilder;
    }

    public FormulaEvaluationResult evaluate(StockFormulaDefinition formula,
                                            StockInfo stock,
                                            List<StockDailyKlineRow> rows,
                                            Integer targetTradeDate)
    {
        if (formula == null || stock == null || rows == null || rows.isEmpty() || targetTradeDate == null)
        {
            return null;
        }

        Kline kline = stockKlineBuilder.build(stock.getCode(), stock.getStockName(), rows);
        if (kline == null)
        {
            return null;
        }

        try
        {
            Interpreter interpreter = new Interpreter(stock.getCode(), kline);
            interpreter.eval(formula.getCode(), 3);

            ZemNumberArray dataArray = ZemNumberArrayUtil.getNumArray(interpreter.getVariable(formula.getName(), null));
            ZemNumberArray dateArray = ZemNumberArrayUtil.getNumArray(interpreter.getVariable("date", null));
            if (dataArray == null || dateArray == null || dataArray.size() == 0 || dateArray.size() == 0)
            {
                return null;
            }

            int lastIndex = Math.min(dataArray.size(), dateArray.size()) - 1;
            int tradeDate = dateArray.get(lastIndex).intValue();
            if (tradeDate != targetTradeDate.intValue())
            {
                return null;
            }
            if (dataArray.get(lastIndex).intValue() != 1)
            {
                return null;
            }
            return new FormulaEvaluationResult(tradeDate, resolveSelectionPrice(rows, tradeDate));
        }
        catch (Exception ex)
        {
            log.error("Formula evaluation failed, strategy={}, stockCode={}", formula.getName(), stock.getCode(), ex);
            throw new BusinessException("formula evaluation failed: " + formula.getName(), ex);
        }
    }

    public FormulaEvaluationResult evaluate60Min(StockFormulaDefinition formula,
                                                 StockInfo stock,
                                                 List<Stock60MinKlineRow> rows,
                                                 Integer targetTradeDate)
    {
        if (formula == null || stock == null || rows == null || rows.isEmpty() || targetTradeDate == null)
        {
            return null;
        }

        Kline kline = stockKlineBuilder.build60Min(stock.getCode(), stock.getStockName(), rows);
        if (kline == null)
        {
            return null;
        }

        try
        {
            Interpreter interpreter = new Interpreter(stock.getCode(), kline);
            interpreter.eval(formula.getCode(), 3);

            ZemNumberArray dataArray = ZemNumberArrayUtil.getNumArray(interpreter.getVariable(formula.getName(), null));
            ZemNumberArray dateArray = ZemNumberArrayUtil.getNumArray(interpreter.getVariable("date", null));
            if (dataArray == null || dateArray == null || dataArray.size() == 0 || dateArray.size() == 0)
            {
                return null;
            }

            int lastIndex = Math.min(dataArray.size(), dateArray.size()) - 1;
            long tradeDate = Math.round(dateArray.get(lastIndex).doubleValue());
            if (Stock60MinSlotSupport.toDailyTradeDate(tradeDate) != targetTradeDate.intValue())
            {
                return null;
            }
            if (dataArray.get(lastIndex).intValue() != 1)
            {
                return null;
            }

            int slotIndex = Stock60MinSlotSupport.resolveSlotIndexByTradeDate(tradeDate);
            if (slotIndex <= 0)
            {
                return null;
            }
            return new FormulaEvaluationResult(
                    Integer.valueOf(targetTradeDate.intValue()),
                    resolve60MinSelectionPrice(rows, tradeDate),
                    Long.valueOf(tradeDate),
                    Integer.valueOf(slotIndex)
            );
        }
        catch (Exception ex)
        {
            log.error("60min formula evaluation failed, strategy={}, stockCode={}", formula.getName(), stock.getCode(), ex);
            throw new BusinessException("formula evaluation failed: " + formula.getName(), ex);
        }
    }

    public Double evaluateDailyVariable(String formulaCode,
                                        String variableName,
                                        StockInfo stock,
                                        List<StockDailyKlineRow> rows,
                                        Integer targetTradeDate)
    {
        if (formulaCode == null || formulaCode.trim().isEmpty() || variableName == null || variableName.trim().isEmpty()
                || stock == null || rows == null || rows.isEmpty() || targetTradeDate == null)
        {
            return null;
        }

        Kline kline = stockKlineBuilder.build(stock.getCode(), stock.getStockName(), rows);
        if (kline == null)
        {
            return null;
        }

        try
        {
            Interpreter interpreter = new Interpreter(stock.getCode(), kline);
            interpreter.eval(formulaCode, 3);

            ZemNumberArray dataArray = ZemNumberArrayUtil.getNumArray(interpreter.getVariable(variableName.trim(), null));
            ZemNumberArray dateArray = ZemNumberArrayUtil.getNumArray(interpreter.getVariable("date", null));
            if (dataArray == null || dateArray == null || dataArray.size() == 0 || dateArray.size() == 0)
            {
                return null;
            }

            int lastIndex = Math.min(dataArray.size(), dateArray.size()) - 1;
            int tradeDate = dateArray.get(lastIndex).intValue();
            if (tradeDate != targetTradeDate.intValue())
            {
                return null;
            }
            return Double.valueOf(dataArray.get(lastIndex).doubleValue());
        }
        catch (Exception ex)
        {
            log.error("Formula variable evaluation failed, variable={}, stockCode={}", variableName, stock.getCode(), ex);
            throw new BusinessException("formula variable evaluation failed: " + variableName, ex);
        }
    }

    private Double resolveSelectionPrice(List<StockDailyKlineRow> rows, int tradeDate)
    {
        for (int i = rows.size() - 1; i >= 0; i--)
        {
            StockDailyKlineRow row = rows.get(i);
            if (row.getTradeDate() != null && row.getTradeDate().intValue() == tradeDate)
            {
                return row.getClose();
            }
        }
        return null;
    }

    private Double resolve60MinSelectionPrice(List<Stock60MinKlineRow> rows, long tradeDate)
    {
        for (int i = rows.size() - 1; i >= 0; i--)
        {
            Stock60MinKlineRow row = rows.get(i);
            if (row.getTradeDate() != null && row.getTradeDate().longValue() == tradeDate)
            {
                return row.getClose();
            }
        }
        return null;
    }
}
