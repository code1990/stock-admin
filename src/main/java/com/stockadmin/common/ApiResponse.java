package com.stockadmin.common;

public class ApiResponse<T>
{
    private final int code;
    private final String msg;
    private final T data;

    private ApiResponse(int code, String msg, T data)
    {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data)
    {
        return new ApiResponse<T>(200, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String msg)
    {
        return new ApiResponse<T>(code, msg, null);
    }

    public int getCode()
    {
        return code;
    }

    public String getMsg()
    {
        return msg;
    }

    public T getData()
    {
        return data;
    }
}
