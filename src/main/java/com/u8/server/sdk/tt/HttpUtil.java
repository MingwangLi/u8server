package com.u8.server.sdk.tt;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * post请求
 *
 * @author TT
 *
 */
public final class HttpUtil {

    public static String doPost(String url, String urldata, Map<String, Object> headers)
            throws ClientProtocolException, IOException {

        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(urldata, ContentType.APPLICATION_JSON);
        stringEntity.setContentEncoding("UTF-8");

        Set<String> keySet = headers.keySet();
        for (String itemKey : keySet) {
            httpPost.addHeader(itemKey, String.valueOf(headers.get(itemKey)));
        }

        httpPost.setEntity(stringEntity);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpPost);

        HttpEntity entity = response.getEntity();

        String httpStr = EntityUtils.toString(entity);

        return httpStr;
    }

}
