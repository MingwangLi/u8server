package com.u8.server.service;

import com.u8.server.dao.UDeviceDao;
import com.u8.server.data.UDevice;
import com.u8.server.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by ant on 2016/8/12.
 */
@Service("deviceManager")
public class UDeviceManager {

    @Autowired
    private UDeviceDao deviceDao;

    
    public void saveDevice(UDevice device){
        deviceDao.save(device);
    }

    public UDevice getByDeviceID(String deviceID, Integer appID){
        String hql = "from UDevice where deviceID = ? and appID = ?";
        return (UDevice)deviceDao.findUnique(hql, deviceID, appID);
    }
}
