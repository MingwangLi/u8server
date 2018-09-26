package com.u8.server.sdk.kumi;

/**
 * Created by lizhong on 2017/07/24.
 */
public class KMPayResult {
    private String cp_order_id;
    private String mem_id;
    private String order_id;
    private String order_status;
    private String pay_time;
    private String product_id;
    private String product_name;
    private String product_price;
    private String sign;
    private String ext;

    public String getCp_order_id() {
        return cp_order_id;
    }

    public void setCp_order_id(String cp_order_id) {
        this.cp_order_id = cp_order_id;
    }

    public String getMem_id() {
        return mem_id;
    }

    public void setMem_id(String mem_id) {
        this.mem_id = mem_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_price() {
        return product_price;
    }

    public void setProduct_price(String product_price) {
        this.product_price = product_price;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    @Override
    public String toString() {
        return "KMPayResult{" +
                "cp_order_id='" + cp_order_id + '\'' +
                ", mem_id='" + mem_id + '\'' +
                ", order_id='" + order_id + '\'' +
                ", order_status='" + order_status + '\'' +
                ", pay_time='" + pay_time + '\'' +
                ", product_id='" + product_id + '\'' +
                ", product_name='" + product_name + '\'' +
                ", product_price='" + product_price + '\'' +
                ", sign='" + sign + '\'' +
                ", ext='" + ext + '\'' +
                '}';
    }
}
