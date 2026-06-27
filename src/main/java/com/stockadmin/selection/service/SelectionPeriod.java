package com.stockadmin.selection.service;

import com.stockadmin.common.BusinessException;

public final class SelectionPeriod
{
    public static final String PERIOD_240 = "240min";
    public static final String PERIOD_60 = "60min";

    private SelectionPeriod()
    {
    }

    public static String normalize(String period)
    {
        if (period == null || period.trim().isEmpty())
        {
            return PERIOD_240;
        }
        String value = period.trim().toLowerCase();
        if ("240".equals(value) || "240min".equals(value) || "day".equals(value) || "daily".equals(value))
        {
            return PERIOD_240;
        }
        if ("60".equals(value) || "60min".equals(value))
        {
            return PERIOD_60;
        }
        throw new BusinessException("unsupported selection period: " + period);
    }

    public static boolean isSixtyMin(String period)
    {
        return PERIOD_60.equals(normalize(period));
    }
}
