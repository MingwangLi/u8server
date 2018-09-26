package com.u8.server.test;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.model.PutObjectRequest;

import java.io.FileInputStream;
import java.io.InputStream;

public class OSSTest{









        public static void main(String[] args) throws Exception{
            String endpoint = "oss-cn-hangzhou.aliyuncs.com";
// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建
            String accessKeyId = "LTAITywmvKohU1Xn";
            String accessKeySecret = "Or8rzhbULRaQaD0nmJXrpmAvy0cECJ";
// 创建OSSClient实例
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
// 上传文件流
            InputStream inputStream = new FileInputStream("C:\\Users\\123\\Desktop\\神权.apk");
            ossClient.putObject("jmsht-apk", "LTAITywmvKohU1Xn", inputStream);
            inputStream.close();
// 关闭client
            ossClient.shutdown();
        }
}

