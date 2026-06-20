package com.aidex.framework.aspectj;

import com.aidex.common.annotation.DataSource;
import com.aidex.framework.datasource.DynamicDataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(-1)
@Component
public class DataSourceAspect
{
    @Around("@annotation(com.aidex.common.annotation.DataSource) || @within(com.aidex.common.annotation.DataSource)")
    public Object around(ProceedingJoinPoint point) throws Throwable
    {
        DataSource dataSource = resolveDataSource(point);
        if (dataSource != null)
        {
            DynamicDataSourceContextHolder.setDataSourceType(dataSource.value());
        }
        try
        {
            return point.proceed();
        }
        finally
        {
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }

    private DataSource resolveDataSource(ProceedingJoinPoint point)
    {
        MethodSignature signature = (MethodSignature) point.getSignature();
        DataSource methodDataSource = signature.getMethod().getAnnotation(DataSource.class);
        if (methodDataSource != null)
        {
            return methodDataSource;
        }
        return point.getTarget().getClass().getAnnotation(DataSource.class);
    }
}
