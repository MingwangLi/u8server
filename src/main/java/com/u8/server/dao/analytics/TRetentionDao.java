package com.u8.server.dao.analytics;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.analytics.TRetention;
import com.u8.server.utils.TimeUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by ant on 2016/8/29.
 */
@Repository
public class TRetentionDao extends UHibernateTemplate<TRetention, Integer> {

    /**
     * 获取指定游戏指定时间段内的所有统计信息
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public List<TRetention> queryRetentionData(int appID, Date from, Date to){

        String hql = "from TRetention where appID = ?";
        Object[] params = new Object[]{appID};
        if (from != null && to != null){
            hql += " and statTime >= ? and statTime <= ?";
            params = new Object[]{appID, TimeUtils.dateBegin(from), TimeUtils.dateEnd(to)};

        }

        return super.find(hql, params, null);
    }

    public void addDayRetention(int appID) {
       /* Session session = this.getSession();
        //INSERT INTO tretention(appID,dayRetention,statTime,opTime) VALUES(gameID, '', DATE_SUB(CURDATE(),interval 1 day), NOW());
        String sql = "insert into tretention(appID,dayRetention,statTime,opTime) VALUES("+appID+",'',DATE_SUB(CURDATE(),interval 1 day), NOW()";
        SQLQuery query = session.createSQLQuery(sql);
        query.executeUpdate();*/
    }

    public void collectDayRetention(int appID) {

    }

    public List<Object[]> getLTVData(int appID,Date beginTime,Date endTime) {
        String sql = "select ltv,statTime from TRetention where appID = ? and statTime >= ? and statTime <= ?";
        Query query = this.getSession().createQuery(sql);
        query.setParameter(0,appID);
        query.setParameter(1,beginTime);
        query.setParameter(2,endTime);
        return query.list();
    }
}
