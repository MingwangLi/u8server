package com.u8.server.dao;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.UUser;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户数据访问类
 */
@Repository("uUserDao")
public class UUserDao extends UHibernateTemplate<UUser, Integer>{

    
    public void updateUser(Integer userID) {
        Query query = this.getSession().createQuery("update UUser set isCreatedRole = 1 where id = ?");
        query.setParameter(0,userID);
        query.executeUpdate();
    }


}
