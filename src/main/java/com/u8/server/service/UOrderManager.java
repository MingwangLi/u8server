package com.u8.server.service;

import com.u8.server.common.*;
import com.u8.server.constants.PayState;
import com.u8.server.dao.UOrderDao;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.log.Log;
import com.u8.server.utils.IDGenerator;
import com.u8.server.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service("orderManager")
public class UOrderManager {

    @Autowired
    private UOrderDao orderDao;

    public UOrder getOrder(long orderID) {
        return orderDao.get(orderID);
    }

    
    public void saveOrder(UOrder order) {
        orderDao.save(order);
    }

    
    public void deleteOrder(UOrder order) {
        orderDao.delete(order);
    }

    
    public UOrder generateOrder(UUser user, int money, String productID, String productName, String productDesc, String roleID, String roleName, String serverID, String serverName, String extension, String notifyUrl) {
        UOrder order = new UOrder();
        order.setOrderID(IDGenerator.getInstance().nextOrderID());
        order.setAppID(user.getAppID());
        order.setChannelID(user.getChannelID());
        order.setMoney(money*100);
        order.setProductID(productID);
        order.setProductName(productName);
        order.setProductDesc(productDesc);
        order.setCurrency("RMB");
        order.setUserID(user.getId());
        order.setUsername(user.getName());
        order.setExtension(extension);
        order.setState(PayState.STATE_PAYING);
        order.setChannelOrderID("");
        order.setRoleID(roleID);
        order.setRoleName(roleName);
        order.setServerID(serverID);
        order.setServerName(serverName);
        order.setCreatedTime(new Date());
        order.setNotifyUrl(notifyUrl);
        orderDao.save(order);
        return order;
    }

    //分页查找
    public Page<UOrder> queryPage(int currPage, int num) {

        PageParameter page = new PageParameter(currPage, num, true);
        OrderParameters order = new OrderParameters();
        order.add("id", OrderParameter.OrderType.DESC);
        String hql = "from UOrder";
        return orderDao.find(page, hql, null, order);
    }

    //按照条件分页查找
    public Page<UOrder> queryPage(int currPage, int num, Long orderID, Integer appID, Integer channelID,
                                  Integer userID, String username, String productID, String productName,
                                  Integer minMoney, Integer maxMoney, Integer minRealMoney, Integer maxRealMoney, String roleID, String roleName, String serverID,
                                  Integer state, String channelOrderID, Date beginCreateTime, Date endCreateTime, Object[] permisionedGameIDs) {


        SQLParams params = new SQLParams();

        if (orderID != null && orderID > 0) {
            params.EQ("orderID", orderID);
        }

        if (appID != null && appID > 0) {
            params.EQ("appID", appID);
        }
        if (channelID != null && channelID > 0) {
            params.EQ("channelID", channelID);
        }

        if (userID != null && userID > 0) {
            params.EQ("userID", userID);
        }

        if (!StringUtils.isEmpty(username)) {
            params.EQ("username", username);
        }

        if (!StringUtils.isEmpty(productID)) {
            params.EQ("productID", productID);
        }

        if (!StringUtils.isEmpty(productName)) {
            params.EQ("productName", productName);
        }

        if (minMoney != null && minMoney > 0) {
            params.GE("money", minMoney);
        }

        if (maxMoney != null && maxMoney > 0) {
            params.LE("money", maxMoney);
        }

        if (minRealMoney != null && minRealMoney > 0) {
            params.GE("realMoney", minRealMoney);
        }

        if (maxRealMoney != null && maxRealMoney > 0) {
            params.LE("realMoney", maxRealMoney);
        }

        if (!StringUtils.isEmpty(roleID)) {
            params.EQ("roleID", roleID);
        }

        if (!StringUtils.isEmpty(roleName)) {
            params.EQ("roleName", roleName);
        }

        if (!StringUtils.isEmpty(serverID)) {
            params.EQ("serverID", serverID);
        }

        if (state != null && state >= 0) {
            params.EQ("state", state);
        }

        if (!StringUtils.isEmpty(channelOrderID)) {
            params.EQ("channelOrderID", channelOrderID);
        }

        if (beginCreateTime != null) {
            params.GE("createdTime", beginCreateTime);
        }

        if (endCreateTime != null) {
            params.LE("createdTime", endCreateTime);
        }

        if (permisionedGameIDs != null) {
            if (permisionedGameIDs.length == 0) {
                permisionedGameIDs = new Object[]{0};
            }
            params.IN("appID", permisionedGameIDs);
        }

        PageParameter page = new PageParameter(currPage, num, true);
        String hql = "from UOrder" + params.getWhereSQL();
        hql = hql + " order by createdTime desc";
        return orderDao.find(page, hql, params.getWhereValues(), null);
    }

    //获取今日订单
    public List<UOrder> getTodayOrders(Date date) {
        return orderDao.getTodayOrders(date);
    }

    public List<UOrder> download(Integer appID,Integer channelID,Integer state,Date beginCreateTime,Date endCreateTime) {
        return orderDao.getOrdersByConditions(appID,channelID,state,beginCreateTime,endCreateTime);
    }

    public UOrder getOrderByChannelOrderID(String channelOrderID) {
        return orderDao.getOrderByChannelOrderID(channelOrderID);
    }

}
