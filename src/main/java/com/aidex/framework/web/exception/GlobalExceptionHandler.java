package com.aidex.framework.web.exception;

import com.aidex.common.core.domain.AjaxResult;
import com.aidex.common.exception.ServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException ex)
    {
        return AjaxResult.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception ex)
    {
        return AjaxResult.error(ex.getMessage());
    }
}
