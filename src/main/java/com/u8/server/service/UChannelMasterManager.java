package com.u8.server.service;

import com.u8.server.cache.CacheManager;
import com.u8.server.common.Page;
import com.u8.server.common.PageParameter;
import com.u8.server.common.SQLParams;
import com.u8.server.dao.UChannelMasterDao;
import com.u8.server.data.UChannel;
import com.u8.server.data.UChannelMaster;
import com.u8.server.utils.IDGenerator;
import com.u8.server.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 渠道商管理
 */
@Service("channelMasterManager")
public class UChannelMasterManager {

    @Autowired
    private UChannelMasterDao channelMasterDao;

    @Transactional
    public void saveChannelMaster(UChannelMaster master){

        if(master == null){
            return;
        }

        if(master.getMasterID() == null || master.getMasterID() <= 0){
            master.setMasterID(IDGenerator.getInstance().nextMasterID());
            CacheManager.getInstance().addMaster(master);
        }else {
            CacheManager.getInstance().saveMaster(master);
        }
        channelMasterDao.saveChannelMaster(master);
    }

    //删除渠道商
    @Transactional
    public void deleteChannelMaster(UChannelMaster master){
        if(master == null){
            return;
        }

        CacheManager.getInstance().removeMaster(master.getMasterID());
        channelMasterDao.delete(master);
    }

    @Transactional
    public UChannelMaster generateMaster(String name, String masterName, String suffix, String authUrl, String paycallbackUrl, String verifyClass){

        return generateMaster(name,masterName,suffix,authUrl,paycallbackUrl,verifyClass, "");
    }

    //生成渠道商
    @Transactional
    public UChannelMaster generateMaster(String name, String masterName, String suffix, String authUrl, String paycallbackUrl, String verifyClass, String orderUrl){

        UChannelMaster master = new UChannelMaster();
        master.setSdkName(name);
        master.setMasterName(masterName);
        master.setNameSuffix(suffix);
        master.setAuthUrl(authUrl);
        master.setPayCallbackUrl(paycallbackUrl);
        master.setVerifyClass(verifyClass);
        master.setOrderUrl(orderUrl);

        saveChannelMaster(master);

        return master;

    }

    public UChannelMaster queryChannelMaster(int masterID){

        return CacheManager.getInstance().getMaster(masterID);
    }

    public UChannelMaster getMasterByName(String name){
        return CacheManager.getInstance().getMasterByName(name);
    }

    public String getMasterByNameList(String name) {
        return CacheManager.getInstance().getMasterByNameList(name);
    }

    //获取当前指定master下的所有渠道
    public List<UChannel> queryChannels(int masterID){

        List<UChannel> channels = CacheManager.getInstance().getChannelList();
        List<UChannel> lst = new ArrayList<UChannel>();
        for(UChannel c : channels){
            if(c.getMasterID() == masterID){
                lst.add(c);
            }
        }
        return lst;
    }

    //获取所有渠道商信息
    public List<UChannelMaster> queryAll(){

        return CacheManager.getInstance().getMasterList();
    }

    public int getMasterCount(){

        return CacheManager.getInstance().getMasterList().size();
    }


    //分页查找
    public List<UChannelMaster> queryPage(int currPage, int num){

        List<UChannelMaster> masters = CacheManager.getInstance().getMasterList();

        Collections.sort(masters, new Comparator<UChannelMaster>() {
            @Override
            public int compare(UChannelMaster o1, UChannelMaster o2) {
                return o1.getMasterID() - o2.getMasterID();
            }
        });

        int fromIndex = (currPage-1) * num;

        if(fromIndex >= masters.size()){

            return null;
        }

        int endIndex = Math.min(fromIndex+num, masters.size());

        return masters.subList(fromIndex, endIndex);
    }


    //分页查找，直接从数据库查找
    public Page<UChannelMaster> queryPage(int currPage, int num, Integer masterID, String sdkName, String masterName, String nameSuffix,
                                 String verifyClass) {

        SQLParams params = new SQLParams();
        if(masterID != null && masterID > 0){
            params.EQ("masterID", masterID);
        }

        if(!StringUtils.isEmpty(sdkName)){
            params.Like("sdkName", sdkName);
        }

        if(!StringUtils.isEmpty(masterName)){
            params.Like("masterName", masterName);
        }

        if(!StringUtils.isEmpty(nameSuffix)){
            params.Like("nameSuffix", nameSuffix);
        }

        if(!StringUtils.isEmpty(verifyClass)){
            params.Like("verifyClass", verifyClass);
        }

        PageParameter page = new PageParameter(currPage, num, true);
        String hql = "from UChannelMaster" + params.getWhereSQL();

        return channelMasterDao.find(page, hql, params.getWhereValues(), null);

    }

}
