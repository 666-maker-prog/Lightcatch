package com.lightcatch.common.exception;

public class BusinessException extends RuntimeException {
    private int code = 500;
    public BusinessException(String msg) { super(msg); }
    public BusinessException(int code, String msg) { super(msg); this.code = code; }
    public int getCode() { return code; }
}
