package com.u8.server.web.test;


import com.u8.server.common.UActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Namespace("/apk")
public class TestHeader03 extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(TestHeader03.class);

    @Action("/test03")
    public void test03() {
        InputStream inputStream = null;
        String read = null;
        try {
            inputStream = request.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            while((read = bufferedReader.readLine()) != null) {
                sb.append(read);
            }
            logger.debug("----读取的参数:{}",sb.toString());
            inputStream.close();
            bufferedReader.close();
            response.getWriter().print(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
