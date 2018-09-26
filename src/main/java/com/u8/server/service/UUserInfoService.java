package com.u8.server.service;

import com.u8.server.common.Page;
import com.u8.server.dao.UUserInfoDao;
import com.u8.server.data.analytics.UUserInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class UUserInfoService {

    @Autowired
    private UUserInfoDao uUserInfoDao;

    public void save(UUserInfo userInfo) {
        uUserInfoDao.save(userInfo);
    }

    public UUserInfo getByUserIDAndRoleIDAndServerID(Integer usesID,String roleID,String ServerID) {
        return uUserInfoDao.getByUserIDAndRoleID(usesID,roleID,ServerID);
    }

    public void updateRoleLever(UUserInfo uUserInfo) {
       uUserInfoDao.save(uUserInfo);
    }

    public Page<UUserInfo> selectWithPage(Integer appID,Integer userID,String beginTime,String endTime,Integer page,Integer rows,String roleName) throws Exception{
        Page<UUserInfo> list = new Page<>();
        Long total = uUserInfoDao.countWithCondition(appID,userID,beginTime,endTime,roleName);
        list.setTotalCount(total);
        List<UUserInfo> uUserInfoList = uUserInfoDao.selectList(appID,userID,beginTime,endTime,page,rows,roleName);
        list.setResultList(uUserInfoList);
        return list;
    }
}
