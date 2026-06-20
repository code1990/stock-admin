package com.aidex.common.core.controller;

import com.aidex.common.core.domain.AjaxResult;

public class BaseController
{
    protected AjaxResult success(Object data)
    {
        return AjaxResult.success(data);
    }

    protected AjaxResult success(String message, Object data)
    {
        return AjaxResult.success(message, data);
    }
}
