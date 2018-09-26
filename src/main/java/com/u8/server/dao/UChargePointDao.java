package com.u8.server.dao;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.ChargePoint;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/5/7 15:36
 * @Modified:
 */
@Repository("uChargePointDao")
public class UChargePointDao extends UHibernateTemplate<ChargePoint,Integer> {
    public ChargePoint getChargePointByChannelIDAndProductID(Integer channelID,String productID) {
        Session session = this.getSession();
        String hql = "from ChargePoint where channelID = ? and chargeCode = ?";
        Query query = session.createQuery(hql);
        query.setParameter(0,channelID);
        query.setParameter(1,productID);
        List<ChargePoint> list = query.list();
        if (null != list && list.size() >0) {
            return list.get(0);
        }
        return null;
    }
}
