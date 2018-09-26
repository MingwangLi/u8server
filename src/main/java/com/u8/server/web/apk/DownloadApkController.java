package com.u8.server.web.apk;

import com.u8.server.common.UActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

@Namespace("/apk")
public class DownloadApkController extends UActionSupport {

    private String fileName;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private Logger logger = LoggerFactory.getLogger(DownloadApkController.class);

    @SuppressWarnings("all")
    @Action("downloadApk")
    public void downloadApk() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String realPath = request.getRealPath("/");
            for(int i = 0;i < 2;i++ ) {
                File file = new File(realPath);
                realPath = file.getParent();
            }
            String deployedDire = realPath+"/upload/"+fileName;
            logger.debug("----文件下载路径:{}",deployedDire);
            File file = new File(deployedDire);
            if (!file.exists()) {
                logger.warn("----要下载的文件不存在,fileName{}",fileName);
                return;
            }
            inputStream = new FileInputStream(file);
            response.setContentType("application/octet-stream;charset=utf-8");
            response.addHeader("Content-Disposition",   "attachment;filename="+fileName);
            outputStream = response.getOutputStream();
            byte[] arr = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(arr))!= -1) {
                outputStream.write(arr,0,len);
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----文件下载异常,异常信息:{}",e.getMessage());
        }finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }
}
