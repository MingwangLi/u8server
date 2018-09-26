package com.u8.server.dao.analytics;

import com.u8.server.common.OrderParameter;
import com.u8.server.common.OrderParameters;
import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.analytics.ChannelGroupInfo;
import com.u8.server.data.analytics.SummaryInfo;
import com.u8.server.data.analytics.TChannelSummary;
import com.u8.server.utils.TimeUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ant on 2016/8/29.
 */
@Repository
public class TChannelSummaryDao extends UHibernateTemplate<TChannelSummary, Integer> {

    public List<ChannelGroupInfo> queryChannelGroups(int appID, Date from, Date to, OrderParameters orderBy){
        StringBuilder sql = new StringBuilder("select channelID, channelName, sum(deviceNum) deviceNum, sum(userNum) userNum, sum(payUserNum) payUserNum, (sum(money)/100) payMoney from tchannelsummary where appID=?");
        Object[] params = new Object[]{appID};
        if(from != null && to != null){
            sql.append(" and currTime >= ? and currTime <= ?");
            params = new Object[]{appID, TimeUtils.dateBegin(from), TimeUtils.dateEnd(to)};
        }
        sql.append(" GROUP BY channelID ");

        if(orderBy != null){
            sql.append(orderBy.toString());
        }

        SQLQuery q = createSQLQuery(sql.toString(), params);

        List<ChannelGroupInfo> result = new ArrayList<ChannelGroupInfo>();
        List<Object> lst = q.list();
        for(Object c : lst){
            Object[] row = (Object[])c;
            ChannelGroupInfo g = new ChannelGroupInfo();
            g.setChannelID(Integer.valueOf(row[0].toString()));
            g.setChannelName(row[1] == null ? "未知" : row[1].toString());
            g.setDeviceNum(row[2] == null ? 0 : Long.valueOf(row[2].toString()));
            g.setUserNum(row[3] == null ? 0 : Long.valueOf(row[3].toString()));
            g.setPayUserNum(row[4] == null ? 0 : Long.valueOf(row[4].toString()));
            g.setPayMoney(row[5] == null ? 0f : Double.valueOf(row[5].toString()));
            result.add(g);
        }

        return result;

    }


}
