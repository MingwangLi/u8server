package com.u8.server.web.test;


import com.u8.server.common.UActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@Namespace("/apk")
public class TestHeader02 extends UActionSupport {

    private Logger logger = LoggerFactory.getLogger(TestHeader02.class);

    @Action("/test02")
    public void test02() {
        try {
            String name = request.getParameter("name");
            String password = request.getParameter("password");
            logger.debug("----直接取name:{}",name);
            logger.debug("----直接取password:{}",password);
            InputStream inputStream = request.getInputStream();
            //OutputStream outputStream = response.getOutputStream();
            StringBuilder sb = new StringBuilder();
            byte[] arr = new byte[1024];
            int len = 0;
            while((len=inputStream.read(arr))!= -1) {
                sb.append(new String(arr,"UTF-8"));
            }
            inputStream.close();
            logger.debug("----通过流来读取数据:{}",sb.toString());
            PrintWriter writer = response.getWriter();
            writer.print(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
