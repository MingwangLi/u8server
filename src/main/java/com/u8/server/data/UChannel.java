package com.u8.server.data;

import com.u8.server.cache.CacheManager;
import com.u8.server.constants.GlobalConfig;
import net.sf.json.JSONObject;

import javax.persistence.*;

/**
 * 渠道对象(每个游戏对某个渠道)
 */

@Entity
@Table(name = "uchannel")
public class UChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;                 //数据表中ID

    private int channelID;              //渠道ID 和客户端一致
    private int appID;                  //游戏ID
    private int masterID;               //渠道商ID

    private String cpID;                //渠道分配给游戏的cpID
    private String cpAppID;             //渠道分配给游戏的appID
    private String cpAppKey;            //渠道分配给游戏的AppKey
    private String cpAppSecret;         //渠道分配给游戏的AppSecret

    private String cpPayID;             //渠道分配给游戏的支付ID
    private String cpPayKey;            //渠道分配给游戏的支付公钥
    private String cpPayPriKey;         //渠道分配给游戏的支付私钥

    private String cpConfig;            //部分渠道可能有特殊配置信息，设置在该字段中

    private Integer openPayFlag;            //充值功能状态，0：开放；1：关闭。关闭之后，该渠道获取订单号会返回一个错误
    private String chargeCloseTime;         //充值关闭时间     当前时间在此时间范围内,该渠道后去订单时会返回一个错误码

    private Integer version;

    private String lastVersionUrl;

    //下面这几个参数，可以覆盖uchannelmaster中的。
    private String authUrl;         //当前SDK登录认证地址
    private String payCallbackUrl;  //当前SDK支付通知回调地址
    private String verifyClass;     //当前SDK的验证处理类的全类名
    private String orderUrl;        //SDK订单号获取地址，没有则为空

    public UChannelMaster getMaster(){

        return CacheManager.getInstance().getMaster(masterID);
    }

    public UGame getGame(){

        return CacheManager.getInstance().getGame(appID);
    }

    //获取当前渠道的登录认证地址
    public String getChannelAuthUrl(){
        if(this.authUrl == null || this.authUrl.trim().length() == 0){
            return  getMaster().getAuthUrl();
        }
        return this.authUrl;
    }

    //获取当前渠道的SDK处理类的名称
    public String getChannelVerifyClass(){
        if(this.verifyClass == null || this.verifyClass.trim().length() == 0){
            return getMaster().getVerifyClass();
        }
        return this.verifyClass;
    }

    //获取当前渠道的SDK处理类的下单地址
    public String getChannelOrderUrl() {
        if (this.orderUrl == null || this.orderUrl.trim().length() == 0) {
            return getMaster().getOrderUrl();
        }
        return this.orderUrl;
    }

    //获取当前渠道的支付回调地址
    public String getPayCallbackUrl(){
        String baseUrl = getMaster().getPayCallbackUrl();
        if(this.payCallbackUrl != null && this.payCallbackUrl.trim().length() > 0){
            baseUrl = this.payCallbackUrl;
        }

        if (!baseUrl.startsWith("http:")){

            //加上根地址
            if(!baseUrl.startsWith("/")){
                baseUrl = "/" + baseUrl;
            }

            baseUrl = GlobalConfig.BASE_URL + baseUrl;

        }
        /*if(baseUrl.contains(""+channelID)){
            return baseUrl;
        }*/

        if (!baseUrl.endsWith("/")){
            baseUrl += "/";
        }
        return baseUrl + channelID;
    }

    //当前渠道是否开放充值
    public boolean isPayOpen(){

        return this.openPayFlag == 0;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("channelID", channelID);
        json.put("appID", appID);

        UGame game = getGame();

        json.put("appName", game == null ? "" : game.getName());
        json.put("masterID", masterID);

        UChannelMaster master = getMaster();
        json.put("masterName", master == null ? "" : master.getMasterName());
        json.put("cpID", cpID);
        json.put("cpAppID", cpAppID);
        json.put("cpAppKey", cpAppKey);
        json.put("cpAppSecret", cpAppSecret);
        json.put("cpPayID", cpPayID);
        json.put("cpPayKey", cpPayKey);
        json.put("cpPayPriKey", cpPayPriKey);
        json.put("cpConfig",cpConfig);
        json.put("openPayFlag", openPayFlag);
        json.put("authUrl", authUrl);
        json.put("payCallbackUrl", payCallbackUrl);
        json.put("verifyClass", verifyClass);
        json.put("orderUrl", orderUrl);
        json.put("chargeCloseTime",chargeCloseTime);
        json.put("version", version);
        json.put("lastVersionUrl",lastVersionUrl);
        return json;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public int getMasterID() {
        return masterID;
    }

    public void setMasterID(int masterID) {
        this.masterID = masterID;
    }

    public String getCpID() {
        return cpID;
    }

    public void setCpID(String cpID) {
        this.cpID = cpID;
    }

    public String getCpAppID() {
        return cpAppID;
    }

    public void setCpAppID(String cpAppID) {
        this.cpAppID = cpAppID;
    }

    public String getCpAppKey() {
        return cpAppKey;
    }

    public void setCpAppKey(String cpAppKey) {
        this.cpAppKey = cpAppKey;
    }

    public String getCpPayKey() {
        return cpPayKey;
    }

    public void setCpPayKey(String cpPayKey) {
        this.cpPayKey = cpPayKey;
    }

    public String getCpAppSecret() {
        return cpAppSecret;
    }

    public void setCpAppSecret(String cpAppSecret) {
        this.cpAppSecret = cpAppSecret;
    }

    public String getCpConfig() {
        return cpConfig;
    }

    public void setCpConfig(String cpConfig) {
        this.cpConfig = cpConfig;
    }

    public String getCpPayID() {
        return cpPayID;
    }

    public void setCpPayID(String cpPayID) {
        this.cpPayID = cpPayID;
    }

    public String getCpPayPriKey() {
        return cpPayPriKey;
    }

    public void setCpPayPriKey(String cpPayPriKey) {
        this.cpPayPriKey = cpPayPriKey;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public void setPayCallbackUrl(String payCallbackUrl) {
        this.payCallbackUrl = payCallbackUrl;
    }

    public String getVerifyClass() {
        return verifyClass;
    }

    public void setVerifyClass(String verifyClass) {
        this.verifyClass = verifyClass;
    }

    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public Integer getOpenPayFlag() {
        return openPayFlag;
    }

    public void setOpenPayFlag(Integer openPayFlag) {
        this.openPayFlag = openPayFlag;
    }

    public String getChargeCloseTime() {
        return chargeCloseTime;
    }

    public void setChargeCloseTime(String chargeCloseTime) {
        this.chargeCloseTime = chargeCloseTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getLastVersionUrl() {
        return lastVersionUrl;
    }

    public void setLastVersionUrl(String lastVersionUrl) {
        this.lastVersionUrl = lastVersionUrl;
    }
}
