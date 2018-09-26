package com.u8.server.sdk.tt;

public class SDKException extends Exception {

    private int errorCode = -1;
    private String error;
    private static final long serialVersionUID = 1L;

    public SDKException(String paramString) {
        super(paramString);
    }

    public SDKException(Exception paramException) {
        super(paramException);
    }

    public SDKException(String paramString, int paramInt) {
        super(paramString);
        this.errorCode = paramInt;
    }

    public SDKException(String paramString, Exception paramException) {
        super(paramString, paramException);
    }

    public SDKException(String paramString, Exception paramException, int paramInt) {
        super(paramString, paramException);
        this.errorCode = paramInt;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getError() {
        return this.error;
    }
}
