package com.lightcatch.common.model;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;
    private String message;
    private T data;
    private String requestId;

    public static <T> Result<T> ok() { return ok(null, "操作成功"); }
    public static <T> Result<T> ok(T data) { return ok(data, "操作成功"); }
    public static <T> Result<T> ok(T data, String msg) {
        Result<T> r = new Result<>();
        r.success = true;
        r.message = msg;
        r.data = data;
        return r;
    }
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.success = false;
        r.message = msg;
        return r;
    }
    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.success = false;
        r.message = msg;
        return r;
    }
}
