package com.u8.server.web.test;

import com.u8.server.common.UActionSupport;
import com.u8.server.sdk.UHttpAgent;
import net.sf.json.JSONObject;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Namespace("/apk")
public class TestHeader01 extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(TestHeader01.class);

    @Action("/test01")
    public void test01() {
        String url = "http://127.0.0.1:8080/apk/test03";
        String name = "admin";
        String password = "admin";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("password",password);
        Map<String,String> header = new HashMap<>();
        header.put("Content-Type", "text/html");
        //转化为ByteArrayEntity需要通过流来读取
        String response = UHttpAgent.getInstance().post(url,header,new ByteArrayEntity(jsonObject.toString().getBytes(Charset.forName("UTF-8"))));
        logger.debug("----response:{}",response);
    }
}
