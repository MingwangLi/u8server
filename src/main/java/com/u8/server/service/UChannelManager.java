package com.u8.server.service;

import com.u8.server.cache.CacheManager;
import com.u8.server.common.Page;
import com.u8.server.common.PageParameter;
import com.u8.server.common.SQLParams;
import com.u8.server.dao.UChannelDao;
import com.u8.server.data.UChannel;
import com.u8.server.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
@Service("channelManager")
public class UChannelManager {

    @Autowired
    private UChannelDao channelDao;

    
    public UChannel generateChannel(int appID, int masterID, String cpID, String cpAppID, String cpAppKey, String cpAppSecret, String cpPayKey){

        return generateChannel(appID, masterID, cpID, cpAppID, cpAppKey, cpAppSecret, cpPayKey, "", "");
    }

    
    public UChannel generateChannel(int appID, int masterID, String cpID, String cpAppID, String cpAppKey, String cpAppSecret, String cpPayKey, String cpPayPriKey, String cpPayID){

        UChannel channel = new UChannel();
        channel.setAppID(appID);
        channel.setMasterID(masterID);

        channel.setCpID(cpID);
        channel.setCpAppID(cpAppID);
        channel.setCpAppKey(cpAppKey);
        channel.setCpAppSecret(cpAppSecret);
        channel.setCpPayKey(cpPayKey);
        channel.setCpPayPriKey(cpPayPriKey);
        channel.setCpPayID(cpPayID);

        saveChannel(channel);

        return channel;
    }

    public int getChannelCount(){

        return CacheManager.getInstance().getChannelList().size();
    }

    //获取当前一个可用的渠道号，默认算法是获取一个当前最大渠道号+1
    public int getValidChannelID(){

        List<UChannel> lst = CacheManager.getInstance().getChannelList();

        int max = 0;

        for(UChannel c : lst){
            if(c.getChannelID() > max){
                max = c.getChannelID();
            }
        }

        return max+1;
    }

    //分页查找
    public List<UChannel> queryPage(int currPage, int num){

        List<UChannel> channels = CacheManager.getInstance().getChannelList();

        Collections.sort(channels, new Comparator<UChannel>() {
            @Override
            public int compare(UChannel o1, UChannel o2) {
                return o1.getChannelID() - o2.getChannelID();
            }
        });

        int fromIndex = (currPage-1) * num;

        if(fromIndex >= channels.size()){

            return null;
        }

        int endIndex = Math.min(fromIndex + num, channels.size());

        return channels.subList(fromIndex, endIndex);
    }


    //分页查找，直接从数据库查找
    public Page<UChannel> queryPage(int currPage, int num, Integer channelID, Integer appID, Integer masterID, Object[] permissionedGameIDs) {

        SQLParams params = new SQLParams();
        if(masterID != null && masterID != 0){
            params.EQ("masterID", masterID);
        }

        if(channelID != null && channelID > 0){
            params.EQ("channelID", channelID);
        }

        if(appID != null && appID != 0){
            params.EQ("appID", appID);
        }

        if(permissionedGameIDs != null){
            if(permissionedGameIDs.length == 0){
                permissionedGameIDs = new Object[]{0};
            }
            params.IN("appID", permissionedGameIDs);
        }

        PageParameter page = new PageParameter(currPage, num, true);
        String hql = "from UChannel" + params.getWhereSQL();

        return channelDao.find(page, hql, params.getWhereValues(), null);

    }

    //分页查找，直接从数据库查找  模糊搜索
    public Page<UChannel> queryPage(int currPage, int num, Integer channelID, String appIDs, String masterIDs, Object[] permissionedGameIDs) {

        SQLParams params = new SQLParams();
        if(!StringUtils.isEmpty(masterIDs)){
            params.IN("masterID", masterIDs.split(","));
        }

        if(channelID != null && channelID > 0){
            params.EQ("channelID", channelID);
        }

        if(!StringUtils.isEmpty(appIDs)){
            params.IN("appID", appIDs.split(","));
        }

        if(permissionedGameIDs != null){
            if(permissionedGameIDs.length == 0){
                permissionedGameIDs = new Object[]{0};
            }
            params.IN("appID", permissionedGameIDs);
        }

        PageParameter page = new PageParameter(currPage, num, true);
        String hql = "from UChannel" + params.getWhereSQL();

        return channelDao.find(page, hql, params.getWhereValues(), null);

    }


    //添加或者修改channel
  
    public void saveChannel(UChannel channel){

        if(channel.getChannelID() <= 0){
            channel.setChannelID(getValidChannelID());
            CacheManager.getInstance().addChannel(channel);
        }


        channelDao.save(channel);
        CacheManager.getInstance().saveChannel(channel);
    }

    public UChannel queryChannel(int id){

        return CacheManager.getInstance().getChannel(id);
    }

    
    public void deleteChannel(UChannel channel){
        if(channel == null){
            return;
        }
        channelDao.delete(channel);
        CacheManager.getInstance().removeChannel(channel.getChannelID());
    }

    public UChannel getChannelByChannelID(Integer channelID) {
        return channelDao.getByChannelID(channelID);
    }

}
