package com.u8.server.dao;


import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.utils.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 订单数据访问类
 */
@Repository
public class UOrderDao extends UHibernateTemplate<UOrder, Long>{

    public List<UOrder> getTodayOrders(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String time = sdf.format(date);
            String beginTime = time + " 00:00:00";
            String endTime = time + " 23:59:59";
            SimpleDateFormat sdfformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date beginDate = sdfformat.parse(beginTime);
            Date endDate = sdfformat.parse(endTime);
            Session session = getSession();
            String hql = "from UOrder where completeTime >= ? and completeTime <= ? and state = 3";
            Query query = session.createQuery(hql);
            query.setParameter(0, beginDate);
            query.setParameter(1, endDate);
            List<UOrder> list =  query.list();
            return list;
        } catch (Exception e) {
            Log.e(e.getMessage());
        }

        return null;
    }

    public List<UOrder> getOrdersByConditions(Integer appID,Integer channelID,Integer state,Date beginCreateTime,Date endCreateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String beginTime = null;
            String endTime = null;
            if (null != beginCreateTime) {
                beginTime = sdf.format(beginCreateTime);
            }
            if (null != endCreateTime) {
                endTime = sdf.format(endCreateTime);
            }
            String hql = "from UOrder where 1 = 1 ";
            StringBuffer sb = new StringBuffer();
            sb.append(hql);
            if (null != appID) {
                sb.append("and appID = "+appID);
            }
            if(null != channelID) {
                sb.append(" and channelID = "+channelID);
            }
            if (null != state) {
                sb.append(" and state = "+state);
            }
            if (!StringUtils.isEmpty(beginTime)) {
                sb.append(" and createdTime >= '"+beginTime+"'");
            }
            if (!StringUtils.isEmpty(endTime)) {
                sb.append(" and createdTime <= '"+endTime+"'");
            }
            Query query = this.getSession().createQuery(sb.toString());
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e.getMessage());
        }
        return null;
    }

    public UOrder getOrderByChannelOrderID(String channelOrderID) {
        String hql = "from UOrder where channelOrderID = ?";
        Query query = this.getSession().createQuery(hql);
        query.setParameter(0,channelOrderID);
        List<UOrder> list = query.list();
        if (null != list) {
            return list.get(0);
        }
        return null;
    }

}
