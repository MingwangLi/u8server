package com.u8.server.sdk.tc;

/**
 * Created by lvxinmin on 2016/11/15.
 */
public class TCPayCallbackInfo {

    private String user_id;//用户名
    private int game_id;//game_id
    private int coin_amount;//金额
    private String order;//订单号
    private String attch;//扩展字段

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public int getCoin_amount() {
        return coin_amount;
    }

    public void setCoin_amount(int coin_amount) {
        this.coin_amount = coin_amount;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getAttch() {
        return attch;
    }

    public void setAttch(String attch) {
        this.attch = attch;
    }
}
