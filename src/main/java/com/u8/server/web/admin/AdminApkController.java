package com.u8.server.web.admin;



import com.u8.server.cache.CacheManager;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.GlobalConfig;
import com.u8.server.data.UChannel;
import com.u8.server.service.UChannelManager;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts2.convention.annotation.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RequestMapping("/admin")
public class AdminApkController extends UActionSupport {

    //private Integer channelID;

    //private String version;

    private String fileName;

    /*public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }*/

   /* public void setVersion(String version) {
        this.version = version;
    }*/

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Autowired
    private UChannelManager uChannelManager;

    private Logger logger = LoggerFactory.getLogger(AdminApkController.class);
    /**
     * 问题:读取的list为null
     * 原因:在经过拦截器的时候已经被读取了
     * 解决方案:自己实现一个过滤器 配置在strut2过滤器前面 转换request
     */
   /* @Action("/uploadApk")
    public void uploadApk() {
        try {
          InputStream in = apkFile.getInputStream();
          OSSUtils.uploadFile(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static final String endpoint = "oss-cn-hangzhou.aliyuncs.com";
    public static final String accessKeyId = "LTAITywmvKohU1Xn";
    public static final String accessKeySecret = "Or8rzhbULRaQaD0nmJXrpmAvy0cECJ";
    public static final String bucketName = "jmsht-apk";

    @SuppressWarnings("all")
    @Action("uploadApk")
    public void uploadApk() {
        //logger.debug("----文件上传参数channelID:{},version:{}",String.valueOf(channelID),version);
        InputStream inputStream  = null;
        FileOutputStream outputStream = null;
        try {
            /**
             * oss上传
             */
           /* InputStream inputStream = null;
            Long size = null;
            String fileName = "";
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");
            if(!upload.isMultipartContent(request)){
                return ;
            }
            List<FileItem> list = upload.parseRequest(request);
            for (FileItem fileItem:list) {
                if(!fileItem.isFormField()) {
                    fileName = fileItem.getName();
                    size = fileItem.getSize();
                    inputStream = fileItem.getInputStream();
                }
            }
            OSSUtils.uploadFile(inputStream,size,fileName);*/

            /**
             * 本地上传
             */
            Long size = null;
            String channelID = null;
            String version = null;
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");
            if(!upload.isMultipartContent(request)) {
                return;
            }
            List<FileItem> list = upload.parseRequest(request);
            for (FileItem fileItem:list) {
                if(!fileItem.isFormField()) {
                    //fileName = fileItem.getName();
                    size = fileItem.getSize();
                    inputStream = fileItem.getInputStream();
                }else {
                    if ("channelIDApk".equals(fileItem.getFieldName())) {
                        channelID = fileItem.getString();
                        logger.debug("----上传参数channelID:{}",channelID);
                    }else if("version".equals(fileItem.getFieldName())){
                        version = fileItem.getString();
                        logger.debug("----上传参数version:{}",version);
                    }
                }
            }
            String realPath = request.getRealPath("/");
            for(int i = 0;i < 2;i++ ) {
                File file = new File(realPath);
                realPath = file.getParent();
            }
            String fileName = UUID.randomUUID().toString()+".apk";
            String path = realPath+"/upload/"+ fileName;
            logger.debug("----文件上传path:{}",path);
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
            byte[] arr = new byte[1024];
            int len = 0;
            while((len = inputStream.read(arr,0,arr.length)) != -1) {
                outputStream.write(arr,0,len);
            }
            outputStream.flush();
            inputStream.close();
            outputStream.close();
            UChannel uChannel = CacheManager.getInstance().getChannel(Integer.parseInt(channelID));
            uChannel.setVersion(Integer.valueOf(version));
            String url = GlobalConfig.BASE_URL+"/apk/downloadApk?fileName="+fileName;
            uChannel.setLastVersionUrl(url);
            uChannelManager.saveChannel(uChannel);  //此方法已经实现缓存同步
            renderState(true);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----APK文件上传失败,失败信息:"+e.getMessage());
            renderState(false);
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

    /*文件下载 提供给第三方 不需要登陆 需要新增namespace 在web.xml配置*/
     /* @SuppressWarnings("all")
    @Action("/downloadApk")
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
    }*/
    private void renderState(boolean suc) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("msg", suc ? "操作成功" : "操作失败");
        renderText(json.toString());
    }

}
