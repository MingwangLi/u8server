package com.u8.server.web.analytics;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.UGame;
import com.u8.server.data.UUser;
import com.u8.server.data.UUserLog;
import com.u8.server.log.Log;
import com.u8.server.service.UGameManager;
import com.u8.server.service.UUserLogManager;
import com.u8.server.service.UUserManager;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Date;

/**
 * Created by ant on 2016/8/15.
 */
@Controller
@Namespace("/analytics")
public class UserLogAction extends UActionSupport  implements ModelDriven<UUserLog>{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private UUserLog log;
    private String sign;

    @Autowired
    private UUserLogManager userLogManager;

    @Autowired
    private UGameManager gameManager;

    @Autowired
    private UUserManager userManager;

    @Action("addUserLog")
    public void addUserLog() {
        try {
            UGame game = gameManager.queryGame(log.getAppID());
            if (game == null) {
                renderState(false, "游戏不存在");
                return;
            }

            UUser user = userManager.getUser(log.getUserID());
            if (user == null) {
                renderState(false, "玩家不存在");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("appID=").append(log.getAppID())
                    .append("channelID=").append(log.getChannelID())
                    .append("deviceID=").append(log.getDeviceID())
                    .append("opType=").append(log.getOpType())
                    .append("roleID=").append(log.getRoleID())
                    .append("roleLevel=").append(log.getRoleLevel())
                    .append("roleName=").append(log.getRoleName())
                    .append("serverID=").append(log.getServerID())
                    .append("serverName=").append(log.getServerName())
                    .append("userID=").append(log.getUserID())
                    .append(game.getAppkey());

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            if (!sign.equals(this.sign)) {
                renderState(false, "验证失败");
                return;
            }
            this.log.setIp(request.getRemoteAddr());
            this.log.setOpTime(new Date());
            this.log.setRegTime(user.getCreateTime());
            userLogManager.saveUserLog(this.log);
            renderState(true, "上传成功");
        } catch (Exception e) {
            renderState(false, "上传失败");
            logger.error("----上传日志异常,异常信息:{}",e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderState(boolean suc, String msg) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("msg", msg);
        renderText(json.toString());
    }

    @Override
    public UUserLog getModel() {
        if (this.log == null) {
            this.log = new UUserLog();
        }
        return this.log;
    }

    public UUserLog getLog() {
        return log;
    }

    public void setLog(UUserLog log) {
        this.log = log;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
