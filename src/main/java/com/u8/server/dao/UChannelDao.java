package com.u8.server.dao;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.UChannel;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 渠道数据访问类
 */
@Repository
public class UChannelDao extends UHibernateTemplate<UChannel, Integer>{

    public void saveChannel(UChannel channel){
        super.save(channel);
    }

    public UChannel queryChannel(int id){

        return super.get(id);
    }

    public Integer[] findByAppID(Integer appID) {
        String hql = "select channelID from UChannel where appID = ?";
        Query query = this.getSession().createQuery(hql);
        query.setParameter(0,appID);
        List<Integer> uChannelList = query.list();
        Integer[] channelIDs = new Integer[uChannelList.size()];
        if (null != uChannelList) {
            for (int i = 0;i < uChannelList.size();i++) {
                channelIDs[i] = uChannelList.get(i);
            }
            return channelIDs;
        }
       return null;
    }

    public Integer[] findByMasterID(Integer masterID) {
        String hql = "select channelID from UChannel where masterID = ?";
        Query query = this.getSession().createQuery(hql);
        query.setParameter(0,masterID);
        List<Integer> uChannelList = query.list();
        Integer[] channelIDs = new Integer[uChannelList.size()];
        if (null != uChannelList) {
            for (int i = 0;i < uChannelList.size();i++) {
                channelIDs[i] = uChannelList.get(i);
            }
            return channelIDs;
        }
        return null;
    }

    public UChannel getByChannelID(Integer channelID) {
        String hql = "from UChannel where channelID = ?";
        Query query = this.getSession().createQuery(hql);
        query.setParameter(0,channelID);
        List<UChannel> list = query.list();
        if (null != list && list.size() >0) {
            return list.get(0);
        }
        return null;
    }
}
