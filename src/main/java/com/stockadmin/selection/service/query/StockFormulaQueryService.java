package com.stockadmin.selection.service.query;

import com.stockadmin.common.BusinessException;
import com.stockadmin.selection.domain.StockFormulaDefinition;
import com.stockadmin.selection.mapper.StockFormulaMapper;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StockFormulaQueryService
{
    private static final Pattern FORMULA_NAME_PATTERN = Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*:");

    private final StockFormulaMapper stockFormulaMapper;

    public StockFormulaQueryService(StockFormulaMapper stockFormulaMapper)
    {
        this.stockFormulaMapper = stockFormulaMapper;
    }

    public StockFormulaDefinition resolveFormula(String strategyName, String formulaCode)
    {
        if (hasText(formulaCode))
        {
            StockFormulaDefinition definition = new StockFormulaDefinition();
            definition.setName(resolveFormulaName(formulaCode));
            definition.setCode(formulaCode);
            definition.setEnabled(1);
            definition.setPeriodType("DAY");
            return definition;
        }

        StockFormulaDefinition definition = stockFormulaMapper.selectByName(strategyName.trim());
        if (definition == null)
        {
            throw new BusinessException("strategy not found: " + strategyName);
        }
        return definition;
    }

    private String resolveFormulaName(String formulaCode)
    {
        Matcher matcher = FORMULA_NAME_PATTERN.matcher(formulaCode);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        throw new BusinessException("formulaCode must start with formula name, e.g. XG: C > MA(C,5);");
    }

    private boolean hasText(String value)
    {
        return value != null && value.trim().length() > 0;
    }
}
