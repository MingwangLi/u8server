
package com.u8.server.service;

import com.u8.server.common.Page;
import com.u8.server.common.PageParameter;
import com.u8.server.common.SQLParams;
import com.u8.server.dao.UChannelDao;
import com.u8.server.dao.UChannelMasterDao;
import com.u8.server.dao.UChargePointDao;
import com.u8.server.dao.UGameDao;
import com.u8.server.data.ChargePoint;
import com.u8.server.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/5/7 18:30
 * @Modified:
 */

@Service
public class ChargePointManager {

    @Autowired
    private UChargePointDao uChargePointDao;

    @Autowired
    private UGameDao uGameDao;

    @Autowired
    private UChannelDao uChannelDao;

    @Autowired
    private UChannelMasterDao uChannelMasterDao;

    public Page<ChargePoint> getAllChargePointByPage(ChargePoint chargePoint,Integer currentPage,Integer pageSize,String ganme,String channelMaster) {
        SQLParams params = new SQLParams();
        Integer channelID = chargePoint.getChannelID();
        if (null != channelID) {
            params.EQ("channelID",channelID);
        }
        if (!StringUtils.isEmpty(ganme)) {
            Integer appID = uGameDao.findByName(ganme).getAppID();
            Integer[] channelIDS = uChannelDao.findByAppID(appID);
            params.IN("channelID",channelIDS);
        }
        if (!StringUtils.isEmpty(channelMaster)) {
            Integer masterID = uChannelMasterDao.findByName(channelMaster).getMasterID();
            Integer[] channelIDS = uChannelDao.findByMasterID(masterID);
            params.IN("channelID",channelIDS);
        }
        String hql = "from ChargePoint"+params.getWhereSQL();
        PageParameter page = new PageParameter(currentPage,pageSize,true);
        return uChargePointDao.find(page,hql,params.getWhereValues(),null);
    }


    public void save(ChargePoint chargePoint) {
        uChargePointDao.save(chargePoint);
    }


    public void delete(Integer id) {
        uChargePointDao.delete(id);
    }

    public ChargePoint find(Integer id) {
        return uChargePointDao.get(id);
    }
}

