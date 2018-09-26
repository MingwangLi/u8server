package com.u8.server.sdk.papayou;


import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.ISDKOrderListener;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import net.sf.json.JSONObject;

import java.net.URLEncoder;
import java.text.Collator;
import java.util.*;

public class PaPaYouSDK implements ISDKScript {
    //private String userno;
    //private String token;
    //private String apikey;
    //private String platform;
    //private String memberid;
    //private String key;
    //private long timestamp;
    //private String aeskey;
    @Override
    public void verify(UChannel channel, String extension, ISDKVerifyListener callback) throws Exception {
        Date nowTime = new Date();
        String format = DateUtils.formatDate(nowTime, "yyyy-MM-dd HH:mm:ss");
        String key = channel.getCpPayPriKey();
        JSONObject json = JSONObject.fromObject(extension);
        String userno = json.getString("userno");
        String token = json.getString("token");
        String memberid = json.getString("memberid");
        String apikey = channel.getCpAppKey();
        String platform = channel.getCpAppID();
        Long timestamp = System.currentTimeMillis();
        String aeskey = channel.getCpPayKey();
        Map<String, String> map = new HashMap<String, String>();
        map.put("token", token);
        map.put("userno", userno);
        map.put("apikey", apikey);

        String[] arrStrings = { "token", "userno", "apikey" };
        Comparator comparator = Collator.getInstance(java.util.Locale.ENGLISH);
        // 使根据指定比较器产生的顺序对指定对象数组进行排序。
        Arrays.sort(arrStrings, comparator);
        String signSrc = "";
        for (String string : arrStrings) {
            signSrc = signSrc + map.get(string);
        }
        signSrc = signSrc + key;// +DateUtils.date2TimeStamp(format,"yyyy-MM-dd") ;
        String sign = MD5Encrypt.MD5Encode(signSrc);
        String str = "{\"token\":'" + token + "',\"userno\":'" + userno
                + "',\"apikey\":'" + apikey + "'," + "\"sign\":'" + sign
                + "',\"timestamp\":'"
                + DateUtils.date2TimeStamp(format, "yyyy-MM-dd HH:mm:ss")
                + "'}";
        String encryptResultStr = AES.Encrypt(str, channel.getCpPayKey());
        String param = "platform=" + platform + "&param="
                + URLEncoder.encode(encryptResultStr, "UTF-8");
        String urlNameString = channel.getChannelAuthUrl() + "cpApi/secondCheckMember?";
        String sr = HttpRequest.sendPost(urlNameString, param);
        JSONObject result = JSONObject.fromObject(sr);
        String code = result.getString("code");
        if ("0".equals(code)){
            callback.onSuccess(new SDKVerifyResult(true,memberid,userno,userno));
            return;
        }
        callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the post result is " + sr);
    }
    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) throws Exception {
        String url = user.getChannel().getChannelOrderUrl();
        Date nowTime = new Date();
        String format = DateUtils.formatDate(nowTime, "yyyy-MM-dd HH:mm:ss");
        String key = user.getChannel().getCpPayPriKey();//md5Key(私钥)
        String apikey = user.getChannel().getCpAppKey();//apikey
        String aes_key = user.getChannel().getCpPayKey();//AESKEY
        String platform = user.getChannel().getCpAppID();//AccountNumber;
        String totalfee = order.getMoney()/100 +"";
        String outorderno = order.getOrderID()+ "";
        String submittime = DateUtils.date2TimeStamp(format, "yyyyMMddHHmmss");
        String expiretime = DateUtils.date2TimeStamp(format, "yyyyMMddHHmmss");
        String delivertype = "0";
        String dealprice = order.getMoney()/100 +"";
        String playerid = user.getChannelUserID();
        String notifyurl = user.getChannel().getPayCallbackUrl();
        String subject = order.getProductName();
        String gameid = user.getChannel().getCpID();
        String sdktype="1";//区分Android和IOS类型的参数 Android=1 IOS=2 IOS越狱=3

        //角色信息
        String sleve = "1";//等级
        String sgameplay = order.getServerName();//区服名称
        String srole = order.getRoleName();//角色
        String roleid = order.getRoleID();//角色id
        String serviceid = order.getServerID();//服务ID
        String levename = "等级无";//等级名称
        String sex = "1";//性别 '1 男 0 女 2未知'

        Map<String, String> map = new HashMap<String, String>();
        map.put("totalfee", totalfee);
        map.put("outorderno", outorderno);
        map.put("submittime", submittime);
        map.put("expiretime", expiretime);
        map.put("delivertype", delivertype);
        map.put("dealprice", dealprice);
        map.put("playerid", playerid);
        map.put("notifyurl", notifyurl);
        map.put("subject", subject);
        map.put("apikey", apikey);
        map.put("gameid", gameid);
        map.put("sdktype", sdktype);

        map.put("sleve", sleve);
        map.put("sgameplay", sgameplay);
        map.put("srole", srole);
        map.put("roleid", roleid);
        map.put("serviceid", serviceid);
        map.put("levename", levename);
        map.put("sex", sex);
        String[] arrStrings = { "totalfee", "outorderno", "sdktype", "submittime",
                "expiretime", "delivertype", "dealprice", "playerid",
                "notifyurl", "subject", "apikey", "gameid",
                "sleve", "sgameplay", "srole", "roleid","serviceid", "levename","sex"
        };
        Comparator comparator = Collator.getInstance(java.util.Locale.ENGLISH);
        // 使根据指定比较器产生的顺序对指定对象数组进行排序。
        Arrays.sort(arrStrings, comparator);
        String signSrc = "";
        String parm="";
        for (String string : arrStrings) {
            signSrc = signSrc + map.get(string);
            parm=parm+"="+string;
        }
        signSrc = signSrc + key;
        String sign = MD5Encrypt.MD5Encode(URLEncoder.encode(signSrc.toString(), "UTF-8"));
        String params = "{\"totalfee\":'" + totalfee + "',\"outorderno\":'"
                + outorderno + "'" + ",\"submittime\":'" + submittime + "'"
                + ",\"expiretime\":'" + expiretime + "'"
                + ",\"delivertype\":'" + delivertype + "'"
                + ",\"dealprice\":'" + dealprice + "'" + ",\"playerid\":'"
                + playerid + "', " + "\"gameid\":'" + gameid + "',"
                + "\"notifyurl\":'" + notifyurl + "',\"subject\":'"
                + subject + "',"+ "\"sdktype\":'" + sdktype +"',"
                + "\"sleve\":'" + sleve + "'," + "\"sgameplay\":'" + sgameplay+ "',"
                + "\"srole\":'" + srole + "'," + "\"roleid\":'" + roleid+ "',"
                + "\"serviceid\":'" + serviceid + "'," + "\"levename\":'" + levename+ "',"
                + "\"sex\":'" + sex + "',"
                + "\"apikey\":'" + apikey + "'," + "\"sign\":'" + sign
                + "',\"timestamp\":'"
                + DateUtils.date2TimeStamp(format, "yyyy-MM-dd HH:mm:ss")
                + "'}";
        String encryptResultStr = AES.Encrypt(params, aes_key); //AESKEY
        String param = "platform=" + platform + "&param=" + URLEncoder.encode(encryptResultStr, "UTF-8");
        String urlNameString = url + "cpApi/createOrder?";
        String sr = HttpRequest.sendPost(urlNameString, param);
        JSONObject json = JSONObject.fromObject(sr);
        String code = json.getString("code");
        String msg = json.getString("msg");
        if("0".equals(code)){
            callback.onSuccess(json.getString("data"));
            return;
        }
        callback.onFailed(msg);
    }

}
