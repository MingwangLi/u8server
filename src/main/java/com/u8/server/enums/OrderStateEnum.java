package com.u8.server.enums;

public enum OrderStateEnum {

    NOPAY(1,"未支付"),PAYSUCCESS(2,"支付成功"),COMPLETE(3,"交易完成");

    private int code;
    private String value;

    OrderStateEnum(int code,String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
