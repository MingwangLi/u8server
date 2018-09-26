package com.u8.server.dao.analytics;

import com.u8.server.common.*;
import com.u8.server.data.analytics.SummaryInfo;
import com.u8.server.data.analytics.TSummary;
import com.u8.server.data.analytics.UChannelNewRegData;
import com.u8.server.data.analytics.UChannelProfitData;
import com.u8.server.utils.TimeUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ant on 2016/8/26.
 */
@Repository
public class TSummaryDao extends UHibernateTemplate<TSummary, Integer> {


    /**
     * 获取指定游戏指定时间段内的概要信息
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public SummaryInfo querySummaryInfo(int appID, Date from, Date to){

        String sql = "select sum(deviceNum), sum(userNum), sum(payUserNum), sum(money) from tsummary where appID=?";
        //sql += " and currTime >= '"+ TimeUtils.format_default(from)+"' and currTIme <= '"+TimeUtils.format_default(to) + "'";
        Object[] params = new Object[]{appID};
        if(from != null && to != null){
            sql += " and currTime >= ? and currTime <= ?";
            params = new Object[]{appID, TimeUtils.dateBegin(from), TimeUtils.dateEnd(to)};
        }
        Query q = createSQLQuery(sql, params);
        Object[] result = (Object[])q.uniqueResult();

        SummaryInfo summary = new SummaryInfo();
        summary.setDeviceNum(result[0] == null ? 0 : Long.valueOf(result[0].toString()));
        summary.setUserNum(result[1] == null ? 0 : Long.valueOf(result[1].toString()));
        summary.setPayUserNum(result[2] == null ? 0 : Long.valueOf(result[2].toString()));
        summary.setPayMoney(result[3] == null ? 0 : Long.valueOf(result[3].toString()));
        summary.setSummaryData(querySummaryData(appID, from, to));

        return summary;

    }

    /**
     * 获取指定游戏指定时间段内的所有统计信息
     * @param appID
     * @param from
     * @param to
     * @return
     */
    public List<TSummary> querySummaryData(int appID, Date from, Date to){

        String hql = "from TSummary where appID = ?";
        Object[] params = new Object[]{appID};
        if (from != null && to != null){
            hql += " and currTime >= ? and currTime <= ?";
            params = new Object[]{appID, TimeUtils.dateBegin(from), TimeUtils.dateEnd(to)};

        }

        return super.find(hql, params, null);
    }

    /**
     * 获取指定游戏指定时间段内的渠道收入/付费用户数 数据
     * Modified By lizhong
     * @param appID
     * @param from
     * @param to
     * @param appIDList
     * @return
     */
    public List<UChannelProfitData> queryProfitData(int appID, Date from, Date to, String appIDList){
        String ids = appID + "";
        if(appID == 0){
            ids = appIDList;
        }
        StringBuilder sql = new StringBuilder("select appID,channelID,sum(realMoney)/100 totalMoney,count(distinct(username)) totalCostNum from uorder where appID in (" + ids + ") and  state = 3 ");
        Object[] params = new Object[]{};
        if(from != null && to != null){
            sql.append(" and createdTime >= ? and createdTime <= ?");
            params = new Object[]{TimeUtils.dateBegin(from), TimeUtils.dateEnd(to)};
        }
        sql.append(" group by channelID order by appID,channelID");
        SQLQuery q = createSQLQuery(sql.toString(), params);
        List<UChannelProfitData> currDataList = new ArrayList<UChannelProfitData>();
        List<Object> lst = q.list();
        for(Object c : lst){
            Object[] row = (Object[])c;
            UChannelProfitData data = new UChannelProfitData();
            data.setAppID(Integer.valueOf(row[0].toString()));
            data.setChannelID(Integer.valueOf(row[1].toString()));
            data.setTotalMoney(row[2].toString());
            data.setTotalCostNum(row[3].toString());
            currDataList.add(data);
        }
        return currDataList;
    }
    /**
     * 获取指定游戏指定时间段内的渠道新增数据
     * Modified By lizhong
     * @param appID
     * @param from
     * @param to
     * @param appIDList
     * @return
     */
    public List<UChannelNewRegData> queryNewRegData(int appID, Date from, Date to, String appIDList){
        String ids = appID + "";
        if(appID == 0){
            ids = appIDList;
        }
        StringBuilder sql = new StringBuilder("select appID,channelID,count(1) totalNewreg from uuser where appID in (" + ids + ")");
        Object[] params = new Object[]{};
        if(from != null && to != null){
            sql.append(" and createTime >= ? and createTime <= ?");
            params = new Object[]{ TimeUtils.dateBegin(from), TimeUtils.dateEnd(to)};
        }
        sql.append(" group by channelID order by appID,channelID");
        SQLQuery q = createSQLQuery(sql.toString(), params);

        StringBuilder sql1 = new StringBuilder("select appID,channelID,count(1) from uuser where appID in (" + ids + ")");
        Object[] params1 = new Object[]{};
        if(from != null && to != null){
            sql1.append(" and firstChargeTime  >= ? and firstChargeTime <= ?");
            params1 = new Object[]{ TimeUtils.dateBegin(from), TimeUtils.dateEnd(to)};
        }
        sql1.append(" group by channelID order by appID,channelID");
        SQLQuery q1 = createSQLQuery(sql1.toString(), params1);




        //该时间段注册的玩家首充时间也在该时间段的玩家数
        List<UChannelNewRegData> currDataList = new ArrayList<UChannelNewRegData>();
        List<Object> lst = q.list();
        List<Object> lst1 = q1.list();
        for(Object c : lst){
            Object[] row = (Object[])c;
            UChannelNewRegData data = new UChannelNewRegData();
            data.setAppID(Integer.valueOf(row[0].toString()));
            data.setChannelID(Integer.valueOf(row[1].toString()));
            data.setTotalNewreg(row[2].toString());
            data.setTotalNewCostNum(0 + "");
            for (Object c1 : lst1){
                Object[] row1 = (Object[])c1;
                if (row[0].equals(row1[0]) && row[1].equals(row1[1])){
                    data.setTotalNewCostNum(row1[2].toString());
                }
            }
            currDataList.add(data);
        }
        for (Object c1 : lst1){
            Object[] row1 = (Object[])c1;
            UChannelNewRegData data = new UChannelNewRegData();
            boolean flag = true;
            for(Object c : lst) {
                Object[] row = (Object[])c;
                if (row[0].equals(row1[0]) && row[1].equals(row1[1])) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                data.setAppID(Integer.valueOf(row1[0].toString()));
                data.setChannelID(Integer.valueOf(row1[1].toString()));
                data.setTotalNewreg("0");
                data.setTotalNewCostNum(row1[2].toString());
                currDataList.add(data);
            }
        }
        return currDataList;
    }

    /*-----------------------------*/
    /**
     * 获取指定游戏指定时间段内的渠道新增数据
     * @param appID
     * @param from
     * @param to
     * @param appIDList
     * @return
     */
    public List<UChannelNewRegData> queryCostNum(int appID, Date from, Date to, String appIDList){
        String ids = appID + "";
        if(appID == 0){
            ids = appIDList;
        }
        StringBuilder sql = new StringBuilder("select appID,channelID,count(1) from uuser where appID in (" + ids + ")");
        Object[] params = new Object[]{};
        if(from != null && to != null){
            sql.append(" and createTime >= ? and createTime <= ? and firstChargeTime  >= ? and createTime <= ?");
            params = new Object[]{ TimeUtils.dateBegin(from), TimeUtils.dateEnd(to),TimeUtils.dateBegin(from), TimeUtils.dateEnd(to)};
        }
        sql.append(" group by channelID order by appID");

        SQLQuery q = createSQLQuery(sql.toString(), params);
        List<UChannelNewRegData> currDataList = new ArrayList<UChannelNewRegData>();
        List<Object> lst = q.list();
        for(Object c : lst){
            Object[] row = (Object[])c;
            UChannelNewRegData data = new UChannelNewRegData();
            data.setAppID(Integer.valueOf(row[0].toString()));
            data.setChannelID(Integer.valueOf(row[1].toString()));
            data.setTotalNewreg(row[2].toString());
            currDataList.add(data);
        }
        return currDataList;
    }

    /**
     * 分页查询
     * @param currPage
     * @param num
     * @param appID
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public Page<TSummary> querySummaryPage(int currPage, int num, Integer appID, Date beginCreateTime, Date endCreateTime){

        SQLParams params = new SQLParams();
        if(appID != null && appID > 0){
            params.EQ("appID", appID);
        }

        if(beginCreateTime != null){
            params.GE("currTime", beginCreateTime);
        }
        if(endCreateTime != null){
            params.LE("currTime", endCreateTime);
        }

        String hql = "from TSummary " + params.getWhereSQL();

        PageParameter page = new PageParameter(currPage, num, true);
        OrderParameters order = new OrderParameters();
        order.add("currTime", OrderParameter.OrderType.DESC);

        return super.find(page, hql, params.getWhereValues(), order);

    }

    public List<TSummary> getSummaryPageAll(Integer appID,Date beginTime,Date endTime) {
        String hql = "from TSummary where appID = ?";
        if (null != beginTime) {
            hql += " and currTime >= ?";
        }
        if (null != endTime) {
            hql += " and currTime <= ?";
        }
        hql += " order by currTime desc";
        Query query = this.getSession().createQuery(hql);
        query.setParameter(0,appID);
        if (null != beginTime) {
            query.setParameter(1,beginTime);
        }
        if (null != endTime) {
            query.setParameter(2,endTime);
        }
        List<TSummary> list = query.list();
        if(null != list) {
            return list;
        }
        return null;
    }



}
