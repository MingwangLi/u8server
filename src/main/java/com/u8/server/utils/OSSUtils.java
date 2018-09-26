package com.u8.server.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.u8.server.log.Log;

import java.io.InputStream;


/**
 * Bucket 命名规范：1)只能包括小写字母，数字和短横线（-）；2)必须以小写字母或者数字开头；3)长度必须在 3-63 字节之间。
 */
public class OSSUtils {

    public static final String endpoint = "oss-cn-hangzhou.aliyuncs.com";
    public static final String accessKeyId = "LTAITywmvKohU1Xn";
    public static final String accessKeySecret = "Or8rzhbULRaQaD0nmJXrpmAvy0cECJ";
    public static final String bucketName = "jmsht-apk";

    public static void uploadFile(InputStream inputStream,long size,String fileName) throws Exception{
        String key = fileName.substring(0,fileName.lastIndexOf("-"))+"/"+fileName;
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        if (ossClient.doesObjectExist(bucketName,key)) {
            Log.d("该文件已经存在");
            return ;
        }
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(size);
        meta.setContentType("application/octet-stream");
        ossClient.putObject(bucketName,key,inputStream,meta);
        inputStream.close();
        ossClient.shutdown();
    }
}
