package com.u8.server.test;

import java.net.URLEncoder;

public class JSONObjectTest {

    public static void main(String[] args) throws Exception{
        //%e5%86%92%e9%99%a9%e7%8e%8b2 åé©ç2
        //String value = "åé©ç2";
        String value = "冒险岛2";
        String string = new String(value.getBytes("ISO8859-1"),"UTF-8");
        System.out.println(string);
        System.out.println(URLEncoder.encode(string));

        // String json = "{\"sign\":\"12f21a928ccf9131e520588442deeb3e\",\"id\":1532317006823,\"data\":{\"amount\":\"1.0\",\"callbackInfo\":\"1580401549492879362\",\"failedDesc\":\"\",\"gameId\":\"10008823\",\"orderId\":\"P1807231127268834910\",\"orderStatus\":\"S\",\"payWay\":\"7\",\"roleId\":\"1\",\"serverId\":\"10\",\"suid\":\"1005977321\"}}";
        // JSONObject object = JSONObject.fromObject(json);
        // object = object.getJSONObject("data");
        // System.out.println(object.toString());
        // String string = object.getString("failedDesc");
        // System.out.println(string);
    }
}
