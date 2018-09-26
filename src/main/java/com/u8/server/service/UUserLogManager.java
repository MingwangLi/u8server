package com.u8.server.service;

import com.u8.server.dao.UUserDao;
import com.u8.server.dao.UUserLogDao;
import com.u8.server.data.UDevice;
import com.u8.server.data.UUserLog;
import com.u8.server.data.analytics.UUserInfo;
import com.u8.server.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户日志
 * Created by ant on 2016/8/15.
 */
@Service("userLogManager")
public class UUserLogManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UUserLogDao userLogDao;

    @Autowired
    private UUserDao uUserDao;

    @Autowired
    private UUserInfoService uUserInfoService;
    
    public void saveUserLog(UUserLog log){
        try {
            userLogDao.save(log);
            //用户创建角色之后 更新用户信息
            if (log.getOpType() == 1) {
                Log.i("用户创建角色成功,需要更新用户信息 用户id="+log.getUserID());
                uUserDao.updateUser(log.getUserID());
                UUserInfo uUserInfo = new UUserInfo(log.getUserID(),log.getRoleID(),log.getRoleName(),log.getRoleLevel(),log.getServerID(),log.getServerName(),log.getAppID(),log.getChannelID());
                uUserInfoService.save(uUserInfo);
            }else if (log.getOpType() == 3) {
                UUserInfo uUserInfo = uUserInfoService.getByUserIDAndRoleIDAndServerID(log.getUserID(),log.getRoleID(),log.getServerID());
                if (null != uUserInfo) {
                    uUserInfo.setRoleLevel(log.getRoleLevel());
                    uUserInfoService.updateRoleLever(uUserInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----保存日志,更新用户信息,添加角色信息异常,异常信息:{}",e.getMessage());
        }

    }

}
