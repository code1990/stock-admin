package com.aidex.common.core.domain;

import java.util.HashMap;

public class AjaxResult extends HashMap<String, Object>
{
    private static final long serialVersionUID = 1L;

    public static AjaxResult success()
    {
        return success("success", null);
    }

    public static AjaxResult success(Object data)
    {
        return success("success", data);
    }

    public static AjaxResult success(String message, Object data)
    {
        AjaxResult result = new AjaxResult();
        result.put("code", 200);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    public static AjaxResult error(String message)
    {
        AjaxResult result = new AjaxResult();
        result.put("code", 500);
        result.put("message", message);
        return result;
    }
}
