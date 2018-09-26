package com.u8.server.constants;

/**
 * Created by ant on 2016/10/9.
 */
public class GlobalConfig {

    /**
     * 这个是U8Server部署所在机子上的根地址，如果是外网，比如阿里云服务器，ip地址为124.23.45.89
     * 那么，在上面安装tomcat，默认端口是8080,将U8Server部署在上面之后，
     * 可以将这里BASE_URL设置为 http://124.23.45.89:8080/u8server
     *
     * 这里根地址， 是为了方便配置所有渠道的支付回调地址。 在后台管理系统中渠道商管理中
     * 可以只配置该渠道的相对支付回调处理地址，比如 /pay/uc/payCallback
     *
     * 这样，改了部署位置之后， 这些支付回调地址都不用重新配置，仅仅把这里的根地址改一下即可
     */
    public static final String BASE_URL = "http://127.0.0.1:8080/u8server";

}
