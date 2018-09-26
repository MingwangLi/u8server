package com.u8.server.web.user;

import com.u8.server.cache.SDKCacheManager;
import com.u8.server.common.UActionSupport;
import com.u8.server.constants.StateCode;
import com.u8.server.dao.UUserDao;
import com.u8.server.data.UChannel;
import com.u8.server.data.UChannelMaster;
import com.u8.server.data.UGame;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.ISDKScript;
import com.u8.server.sdk.ISDKVerifyListener;
import com.u8.server.sdk.SDKVerifyResult;
import com.u8.server.service.UChannelManager;
import com.u8.server.service.UGameManager;
import com.u8.server.service.UUserManager;
import com.u8.server.utils.UGenerator;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Date;


/***
 * 用户登录
 */
@Controller
@Namespace("/user")
public class UserAction extends UActionSupport {

    private int appID;
    private int channelID;
    private String extension;
    private String deviceID;
    private int userID;
    private String token;
    private String sign;

    @Autowired
    private UGameManager gameManager;

    @Autowired
    private UChannelManager channelManager;

    @Autowired
    private UUserManager userManager;


    @Autowired
    private UUserDao userDao;

    @Action("getToken")
    public void getLoginToken() {
        Log.i("----------------------Login|Start get Token----------------------");
        Log.i("------------appID : " + appID+"-----------------------------------");
        Log.i("--------channelID : " + channelID+"-------------------------------");
        Log.i("--------extension : " + extension+"-------------------------------");
        Log.i("-----------userID : " + userID+"----------------------------------");
        Log.i("------------token : " + token+"-----------------------------------");
        Log.i("-------------sign : " + sign+"------------------------------------");
        Log.i("-----------------------------------------------------------------");
        try {
            final UGame game = gameManager.queryGame(this.appID);
            if (game == null) {
                renderState(StateCode.CODE_GAME_NONE, null);
                return;
            }
            final UChannel channel = channelManager.queryChannel(this.channelID);
            Log.i("--------------------channel AppID = " + channel.getAppID()+"--------------------");
            if (channel == null) {
                renderState(StateCode.CODE_CHANNEL_NONE, null);
                return;
            }

            if (channel.getAppID() != this.appID) {
                renderState(StateCode.CODE_CHANNEL_NOT_MATCH, null);
                return;
            }

            UChannelMaster master = channel.getMaster();
            if (master == null) {
                renderState(StateCode.CODE_CHANNEL_NONE, null);
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("appID=").append(this.appID)
                    .append("channelID=").append(this.channelID)
                    .append("extension=").append(this.extension).append(game.getAppkey());


            if (1 != appID) {
                if (!userManager.isSignOK(sb.toString(), sign)) {
                    Log.e("the sign is invalid. sign:" + sign);
                    renderState(StateCode.CODE_SIGN_ERROR, null);
                    return;
                }
            }

            if (1 == appID) {
                //测试 不用请求第三方
                try {
                    String channelUserID = "999";
                    UUser existUser = userManager.getUserByCpID(appID,channel.getChannelID(),channelUserID);
                    if (null == existUser) {
                        existUser = new UUser();
                        existUser.setAppID(appID);
                        existUser.setChannelID(channel.getChannelID());
                        existUser.setChannelUserID(channelUserID);
                        existUser.setChannelUserName("test");
                        existUser.setChannelUserNick("test");
                        existUser.setCreateTime(new Date());
                        existUser.setDeviceID("test");
                        existUser.setIsCreatedRole(0);
                        existUser.setLastLoginTime(new Date());
                        String testUser = System.currentTimeMillis()+"";
                        existUser.setName(testUser);
                        existUser.setStatus(1);
                    }else {
                        existUser.setLastLoginTime(new Date());
                    }
                    String token = UGenerator.generateToken(existUser,game.getAppSecret());
                    existUser.setToken(token);
                    userManager.saveUser(existUser);
                    JSONObject data = new JSONObject();
                    data.put("userID", existUser.getId());
                    data.put("sdkUserID", existUser.getChannelUserID());
                    data.put("username", existUser.getName());
                    data.put("sdkUserName", existUser.getChannelUserName());
                    data.put("token", existUser.getToken());
                    data.put("extension", "test");
                    data.put("timestamp", existUser.getLastLoginTime());
                    data.put("version", channel.getVersion()==null?"":channel.getVersion());
                    data.put("lastVersionUrl", channel.getLastVersionUrl()==null?"":channel.getLastVersionUrl());
                    Log.i("登录认证成功 u8server更新用户信息成功 返回数据:"+data.toString());
                    renderState(StateCode.CODE_SUCCESS, data);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    renderState(StateCode.CODE_AUTH_FAILED,null);
                    return;
                }
            }
            ISDKScript verifier = SDKCacheManager.getInstance().getSDKScript(channel);

            if (verifier == null) {
                Log.e("the ISDKScript is not found . channelID : " + channelID);
                renderState(StateCode.CODE_VERIFY_FAILED, null);
                return;
            }

            Log.d("The auth url : " + channel.getChannelAuthUrl());
            Log.d("channelID : " + channel.getChannelID() + ";extension is " + extension);
            Log.d("extension : " + extension);
            Log.i("---------------start verify---------------");
            verifier.verify(channel, extension, new ISDKVerifyListener() {

                @Override
                public void onSuccess(SDKVerifyResult sdkResult) {

                    try {
                        Log.d("--------------------User verify success!!!    Result:"+sdkResult.getUserID()+"--------------------");
                        if (sdkResult.isSuccess() && !StringUtils.isEmpty(sdkResult.getUserID())) {
                            UUser user = userManager.getUserByCpID(channel.getAppID(), channel.getChannelID(), sdkResult.getUserID());
                            String config = channel.getCpConfig();
                            if (config != null && !config.equals("")){
                                String mark[] = config.split(":");
                                if (user == null) {
                                    if(mark[1].equals("1")){
                                        renderState(StateCode.CODE_CHANNEL_REG_FAIL, null);
                                        return ;
                                    }else {
                                        user = userManager.generateUser(channel, sdkResult, deviceID);
                                    }
                                } else {
                                    if (mark[0].equals("1")){
                                        renderState(StateCode.CODE_CHANNEL_LOGIN_FAIL, null);
                                        return ;
                                    }else {
                                        if (0 == user.getStatus()) {
                                            renderState(StateCode.CODE_CHANNEL_NO_ROLE, null);
                                            return ;
                                        }
                                        user.setChannelUserName(sdkResult.getUserName() == null ? "" : sdkResult.getUserName());
                                        user.setChannelUserNick(sdkResult.getNickName() == null ? "" : sdkResult.getNickName());
                                        user.setLastLoginTime(new Date());
                                    }
                                }
                            }else {
                                if (user == null) {
                                    user = userManager.generateUser(channel, sdkResult, deviceID);
                                } else {
                                    if (0 == user.getStatus()) {
                                        renderState(StateCode.CODE_CHANNEL_NO_ROLE, null);
                                        return ;
                                    }
                                    user.setChannelUserName(sdkResult.getUserName() == null ? "" : sdkResult.getUserName());
                                    user.setChannelUserNick(sdkResult.getNickName() == null ? "" : sdkResult.getNickName());
                                    user.setLastLoginTime(new Date());
                                }
                            }
                            user.setToken(UGenerator.generateToken(user, game.getAppSecret()));
                            userManager.saveUser(user);
                            JSONObject data = new JSONObject();
                            data.put("userID", user.getId());
                            data.put("sdkUserID", user.getChannelUserID());
                            data.put("username", user.getName());
                            data.put("sdkUserName", user.getChannelUserName());
                            data.put("token", user.getToken());
                            data.put("extension", sdkResult.getExtension());
                            data.put("timestamp", user.getLastLoginTime());
                            data.put("version", channel.getVersion()==null?0:channel.getVersion());
                            data.put("lastVersionUrl", channel.getLastVersionUrl()==null?"":channel.getLastVersionUrl());
                            Log.i("登录认证成功 u8server更新用户信息成功 返回数据:"+data.toString());
                            renderState(StateCode.CODE_SUCCESS, data);
                        } else {
                            renderState(StateCode.CODE_AUTH_FAILED, null);
                        }
                    } catch (Exception e) {
                        Log.e("异常信息为:"+e.getMessage());
                        renderState(StateCode.CODE_AUTH_FAILED, null);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed(String errorMsg) {
                    Log.e("--------------------The user verify failed. errorMsg:"+errorMsg+"--------------------");
                    renderState(StateCode.CODE_AUTH_FAILED, null);
                }
            });


        } catch (Exception e) {
            Log.e(e.getMessage());
            renderState(StateCode.CODE_AUTH_FAILED, null);
        }
    }

    private void renderState(int state, JSONObject data) {
        try {
            JSONObject json = new JSONObject();
            json.put("state", state);
            json.put("data", data);
            super.renderJson(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e.getMessage());
        }
    }


    /***
     * 上面协议返回客户端之后，开始连接登录游戏服。游戏服可以调用该协议进行再次登录认证。
     * 但是，该步骤是可选的。游戏服务器也可以自己验证token以及token的时效性，这样就不用来
     * U8Server进行再次登录认证了。
     *
     * 服务器自己验证token，根据U8Server分配给每个游戏参数中的AppSecret，按照生成token的
     * 规则，进行验证。同时，需要验证timestamp的时效性
     *
     */
    @Action("verifyAccount")
    public void loginVerify() {
        Log.d("--------------------------------------------  GameServer to U8Server Verify  ------------------------------------------");
        try {
            UUser user = userManager.getUser(this.userID);
            StringBuilder sb = new StringBuilder();
            sb.append("userID=").append(this.userID)
                    .append("token=").append(this.token)
                    .append(user.getGame().getAppkey());
            if (user == null) {
                renderState(StateCode.CODE_USER_NONE, null);
                return;
            }
            if (StringUtils.isEmpty(this.token)) {
                renderState(StateCode.CODE_VERIFY_FAILED, null);
                return;
            }
            if (!userManager.isSignOK(sb.toString(), sign)) {
                renderState(StateCode.CODE_SIGN_ERROR, null);
                return;
            }
            if (!userManager.checkUser(user, token)) {
                renderState(StateCode.CODE_TOKEN_ERROR, null);
                return;
            }
            JSONObject data = new JSONObject();
            data.put("userID", user.getId());
            data.put("username", user.getName());
            data.put("channelID", user.getChannelID());
            renderState(StateCode.CODE_SUCCESS, data);
            Log.d("--------------------------------------------  GameServer to U8Server verify SUCCESS ------------------------------------------");
            return;
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
        renderState(StateCode.CODE_VERIFY_FAILED, null);
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
