package com.stockadmin.selection.service.engine;

public final class Stock60MinSlotSupport
{
    private Stock60MinSlotSupport()
    {
    }

    public static int toDailyTradeDate(long tradeDate)
    {
        if (tradeDate <= 0L)
        {
            return 0;
        }
        if (tradeDate < 100000000L)
        {
            return (int) tradeDate;
        }
        return (int) (tradeDate / 10000L);
    }

    public static int resolveSlotIndexByTradeDate(long tradeDate)
    {
        if (tradeDate <= 0L)
        {
            return 0;
        }
        int hhmm = (int) Math.abs(tradeDate % 10000L);
        if (hhmm >= 1500)
        {
            return 4;
        }
        if (hhmm >= 1400)
        {
            return 3;
        }
        if (hhmm >= 1130)
        {
            return 2;
        }
        if (hhmm >= 1030)
        {
            return 1;
        }
        return 0;
    }
}
