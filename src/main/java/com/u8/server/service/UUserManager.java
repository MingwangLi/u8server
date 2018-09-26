package com.u8.server.service;

import com.u8.server.common.*;
import com.u8.server.dao.UUserDao;
import com.u8.server.data.UChannel;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.sdk.SDKVerifyResult;
import com.u8.server.utils.EncryptUtils;
import com.u8.server.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("userManager")
public class UUserManager {

    @Autowired
    private UUserDao userDao;

    //根据渠道用户ID获取用户信息
    public UUser getUserByCpID(int appID, int channelID, String cpUserID){

        String hql = "from UUser where appID = ? and channelID = ? and channelUserID = ?";

        return (UUser)userDao.findUnique(hql, appID, channelID, cpUserID);

    }

    //获取指定渠道下的所有用户
    public List<UUser> getUsersByChannel(int channelID){
        String hql = "from UUser where channelID = ?";

        return userDao.find(hql, new Object[]{channelID}, null);
    }

    //获取用户数量
    public long getUserCount(){

        String hql = "select count(id) from UUser";
        return userDao.findLong(hql, null);
    }

    //分页查找
    public Page<UUser> queryPage(int currPage, int num){

        PageParameter page = new PageParameter(currPage, num, true);
        OrderParameters order = new OrderParameters();
        order.add("id", OrderParameter.OrderType.DESC);
        String hql = "from UUser";
        return userDao.find(page, hql, null, order);
    }

    public Page<UUser> queryPage(int currPage, int num,
                                 Integer appID, Integer channelID, Integer userID, Date beginCreateTime, Date endCreateTime,
                                 String channelUserID, String channelUserName, String channelNickName, Date beginLoginTime, Date endLoginTime, Object[] permissionedGameIDs){


//        boolean appendAdd = false;
//        List<Object> params = new ArrayList<Object>();
//        StringBuilder sb = new StringBuilder();

        SQLParams params = new SQLParams();
        if(appID != null && appID > 0){
            params.EQ("appID", appID);
        }
        if(channelID != null && channelID > 0){

            params.EQ("channelID", channelID);

        }
        if(null != userID){
            params.EQ("id", userID);
        }
        if(beginCreateTime != null){
            params.GE("createTime", beginCreateTime);
        }
        if(endCreateTime != null){
            params.LE("createTime", endCreateTime);
        }
        if(!StringUtils.isEmpty(channelUserID)){
            params.EQ("channelUserID", channelUserID);
        }
        if(!StringUtils.isEmpty(channelUserName)){
            params.EQ("channelUserName", channelUserName);
        }
        if(!StringUtils.isEmpty(channelNickName)){
            params.EQ("channelUserNick", channelNickName);
        }
        if(beginLoginTime != null){
            params.GE("lastLoginTime", beginLoginTime);
        }
        if(endLoginTime != null){
            params.LE("lastLoginTime", endLoginTime);
        }

        if(permissionedGameIDs != null){
            if(permissionedGameIDs.length == 0){
                permissionedGameIDs = new Object[]{0};
            }
            params.IN("appID", permissionedGameIDs);
        }


        PageParameter page = new PageParameter(currPage, num, true);
//        OrderParameters order = new OrderParameters();
//        order.add("id", OrderParameter.OrderType.DESC);
        String hql = "from UUser" + params.getWhereSQL();

        hql = hql + " order by lastLoginTime desc";
        //Log.d("user search hql is "+hql);
        Log.d(hql);

        return userDao.find(page, hql, params.getWhereValues(), null);

    }

    public List<UUser> queryLastUsers(int max){
        String hql = "from UUser order by id desc limit "+max;
        return userDao.find(hql, new Object[]{}, null);
    }

    //获取用户的渠道分布
    public String queryUserChannels(int appID){

        String sql = "select user.channelID,count(user.id) from UUser user where user.appID=? group by user.channelID ";
        List lst = userDao.find(sql, new Object[]{appID}, null);

        StringBuilder sb = new StringBuilder();
        if(lst != null && lst.size() > 0){
            for(Object item : lst){
                Object[] items = (Object[])item;
                sb.append("['").append(items[0]).append("', ").append(items[1]).append("],");
            }
        }

        if(sb.length() > 0){
            sb.deleteCharAt(sb.length()-1);
        }

        return sb.toString();
    }

    public UUser getUser(int userID){

        return userDao.get(userID);
    }

    //校验sign
    public boolean isSignOK(String signStr, String sign){
        String newSign = EncryptUtils.md5(signStr);
        Log.d("The newSign is "+newSign);
        return newSign.toLowerCase().equals(sign.toLowerCase());
    }

    public boolean checkUser(UUser user, String token){
        long now = System.currentTimeMillis();
        if(!token.equals(user.getToken()) || (now - user.getLastLoginTime().getTime()) > 3600 * 1000){
            return false;
        }
        return true;
    }

    
    public UUser generateUser(UChannel channel, SDKVerifyResult cpUserInfo, String deviceID){

        UUser user = new UUser();
        user.setAppID(channel.getAppID());
        user.setChannelID(channel.getChannelID());
        user.setName(System.currentTimeMillis() + channel.getMaster().getNameSuffix());
        user.setChannelUserID(cpUserInfo.getUserID());
        user.setChannelUserName(cpUserInfo.getUserName() == null ? "" : cpUserInfo.getUserName());
        user.setChannelUserNick(cpUserInfo.getNickName() == null ? "" : cpUserInfo.getNickName());
        Date now = new Date();
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        user.setDeviceID(deviceID);
        user.setIsCreatedRole(0);
        user.setStatus(1);
        userDao.save(user);

        return user;
    }

    
    public void saveUser(UUser user){
        userDao.save(user);
    }

    
    public void deleteUser(UUser user){
        userDao.delete(user);
    }
}
