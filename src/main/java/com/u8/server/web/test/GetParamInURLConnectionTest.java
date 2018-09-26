package com.u8.server.web.test;

import com.u8.server.common.UActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

@Namespace("/apk")
public class GetParamInURLConnectionTest extends UActionSupport {

    @Action("getParamInURLConnection")
    public void  test() throws Exception{
        String contentType = request.getHeader("Content-Type");
        InputStream is = request.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while((line = br.readLine())!=null) {
            sb.append(line);
        }
        br.close();
        is.close();
        String content = sb.toString();
        Map<String,Object> paramMap = request.getParameterMap();
        for (String key:paramMap.keySet()) {
            sb.append(key);
        }
        Collection<Object> collection = paramMap.values();
        for (Object object:collection) {
            String[] param = (String[])object;
            sb.append(param[0]);

        }
        String test = sb.toString();
        response.getWriter().print("haha");
    }
}
