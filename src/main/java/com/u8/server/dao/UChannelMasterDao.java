package com.u8.server.dao;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.UChannelMaster;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 渠道商数据访问类
 */
@Repository
public class UChannelMasterDao extends UHibernateTemplate<UChannelMaster, Integer> {

    public void saveChannelMaster(UChannelMaster master){
        super.save(master);
    }

    public UChannelMaster queryChannelMaster(int channelID){

        return super.get(channelID);
    }

    public UChannelMaster findByName(String name) {
        String hql = "from UChannelMaster where masterName = ?";
        Query query = this.getSession().createQuery(hql);
        query.setParameter(0,name);
        //不适合模糊查询
        //String hql = "from UChannelMaster where masterName like :masterName";
        //query.setString("masterName","%"+name+"%");
        List<UChannelMaster> list = query.list();
        if (null != list) {
            return list.get(0);
        }
        return null;
    }

}
