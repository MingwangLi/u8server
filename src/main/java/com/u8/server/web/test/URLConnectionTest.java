package com.u8.server.web.test;

import com.u8.server.common.UActionSupport;
import org.apache.commons.io.IOUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

@Namespace("/admin")
public class URLConnectionTest extends UActionSupport {

    @Action("testURLConnection")
    public void test() throws Exception{
        URL myUrl = new URL("http://127.0.0.1:8080/apk/getParamInURLConnection");
        URLConnection urlConnection = myUrl.openConnection();
        // 设置doOutput属性为true表示将使用此urlConnection写入数据
        urlConnection.setDoOutput(true);
            // 定义待写入数据的内容类型，我们设置为application/x-www-form-urlencoded类型
        urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
        // 5秒超时
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);
        // 得到请求的输出流对象
        OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
        // 把数据写入请求的Body
        out.write("{\n" +
                "    \"data\":{\n" +
                "        \"orderId\":\"abcf1330\", \n" +
                "        \"gameId\":\"123\", \n" +
                "        \"serverId\":\"654\", \n" +
                "        \"suid\":\"123456\", \n" +
                "\t\t \"roleId\":\"8588\", \n" +
                "        \"payWay\":\"1\", \n" +
                "        \"amount\":\"100.00\", \n" +
                "        \"callbackInfo\":\"custominfo=xxxxx#user=xxxx\", \n" +
                "        \"orderStatus\":\"S\", \n" +
                "        \"failedDesc\":\"\"\n" +
                "    },\n" +
                "    \"sign\":\"e726eb418511372bc3e39f7fa74f4bc6\"\n" +
                "}\n");
        out.flush();
        out.close();
        InputStream inputStream = urlConnection.getInputStream();
        String encoding = urlConnection.getContentEncoding();
        String body = IOUtils.toString(inputStream, encoding);
        System.out.println(body);

    }
}
