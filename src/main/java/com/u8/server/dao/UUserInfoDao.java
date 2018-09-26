package com.u8.server.dao;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.analytics.UUserInfo;
import com.u8.server.utils.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("uUserInfoDao")
public class UUserInfoDao extends UHibernateTemplate<UUserInfo,Integer> {

    public UUserInfo getByUserIDAndRoleID(Integer userID,String roleID,String serverID) {
        Query query = this.getSession().createQuery("from UUserInfo where userID = ? and roleID = ? and serverID = ?");
        query.setParameter(0,userID);
        query.setParameter(1,roleID);
        query.setParameter(2,serverID);
        List<UUserInfo> uUserInfoList = query.list();
        if ((null != uUserInfoList) && (0 != uUserInfoList.size())) {
            return uUserInfoList.get(0);
        }
        return null;
    }

    public Long countWithCondition(Integer appID,Integer userID,String begin,String end,String roleName) {
        StringBuilder hql = new StringBuilder();
        hql.append("select count(*) from UUserInfo where 1 = 1");
        if (null != appID && 0 != appID) {
            hql.append(" and appID = "+appID);
        }
        if (null != userID) {
            hql.append(" and userID = "+userID);
        }
        if (!StringUtils.isEmpty(begin)) {
            hql.append(" and createdTime >= "+"'"+begin+"'");
        }
        if (!StringUtils.isEmpty(end)) {
            hql.append(" and createdTime <= "+"'"+end+"'");
        }
        if (!StringUtils.isEmpty(roleName)) {
            hql.append(" and roleName like "+"'"+"%"+roleName+"%"+"'");
        }
        Query query = this.getSession().createQuery(hql.toString());
        List list = query.list();
        if (null != list) {
            return (Long)list.get(0);
        }
        return null;
    }

    public List<UUserInfo> selectList(Integer appID,Integer userID,String begin,String end,Integer page,Integer rows,String roleName) {
        StringBuilder hql = new StringBuilder();
        hql.append("from UUserInfo where 1 = 1");
        if (null != appID && 0 != appID) {
            hql.append(" and appID = "+appID);
        }
        if (null != userID) {
            hql.append(" and userID = "+userID);
        }
        if (!StringUtils.isEmpty(begin)) {
            hql.append(" and createdTime >= "+"'"+begin+"'");
        }
        if (!StringUtils.isEmpty(end)) {
            hql.append(" and createdTime <= "+"'"+end+"'");
        }
        if (!StringUtils.isEmpty(roleName)) {
            hql.append(" and roleName like "+"'"+"%"+roleName+"%"+"'");
        }
        Query query = this.getSession().createQuery(hql.toString());
        query.setMaxResults(rows);
        query.setFirstResult((page-1)*rows);
        return query.list();
    }

    public List<UUserInfo> selectByUserID(Integer userID) {
        String hql = "from UUserInfo where userID = ?";
        Query query = getSession().createQuery(hql);
        query.setParameter(0,userID);
        return query.list();
    }

}
