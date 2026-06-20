package com.aidex.framework.datasource;

public final class DynamicDataSourceContextHolder
{
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<String>();

    private DynamicDataSourceContextHolder()
    {
    }

    public static void setDataSourceType(String dataSource)
    {
        CONTEXT_HOLDER.set(dataSource);
    }

    public static String getDataSourceType()
    {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceType()
    {
        CONTEXT_HOLDER.remove();
    }
}
