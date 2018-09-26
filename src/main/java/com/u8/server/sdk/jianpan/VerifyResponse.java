package com.u8.server.sdk.jianpan;

/**
 * Created by lvxinmin on 2016/11/10.
 */
public class VerifyResponse {
    private String status;
    private String msg;
    private String userId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
