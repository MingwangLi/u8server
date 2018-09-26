package com.u8.server.service;

import com.u8.server.dao.UAdminDao;
import com.u8.server.data.UAdmin;
import com.u8.server.data.UAdminRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ant on 2015/8/29.
 */

@Service("adminManager")
public class UAdminManager {

    @Autowired
    private UAdminDao adminDao;

    @Autowired
    private UAdminRoleManager adminRoleManager;

    public UAdmin getAdmin(int id){

        return adminDao.get(id);
    }


    public void saveAdmin(UAdmin admin){
        adminDao.save(admin);
    }


    public void deleteAdmin(UAdmin admin){
        adminDao.delete(admin);
    }

    public UAdmin getAdminByUsername(String username){

        return adminDao.getAdminByUsername(username);
    }

    /**
     * 获取拥有指定游戏管理权限的所有管理员
     * @param appID
     * @return
     */
    public List<UAdmin> getAllAdminsByGameID(int appID){
        List<UAdmin> admins = getAllAdmins();
        List<UAdmin> result = new ArrayList<UAdmin>();
        for(UAdmin admin : admins){
            if(admin.getAdminGames() != null){
                String[] gameIDs = admin.getAdminGames().split(",");
                for (String id : gameIDs){
                    int currID = Integer.valueOf(id);
                    if(currID == appID){
                        result.add(admin);
                        break;
                    }
                }
            }

        }

        return result;
    }

    //往指定的管理员身上分配一个游戏权限

    public void addAdminGamePermission(UAdmin admin, int appID){

        if(appID <= 0){
            return;
        }

        if(admin.getAdminGames() == null){
            admin.setAdminGames(appID+"");
        }

        String[] ids = admin.getAdminGames().split(",");
        for(String id : ids){
            int gameID = Integer.valueOf(id);
            if(gameID == appID){
                return;
            }
        }

        admin.setAdminGames(admin.getAdminGames()+","+appID);
        adminDao.save(admin);
    }

    public Object[] getPermissonedGameIDs( UAdmin admin){

        UAdminRole role = adminRoleManager.getAdminRoleByID(admin.getAdminRoleID());

        if(role.getTopRole() != null && role.getTopRole().equals(1)){
            return null;
        }

        if(admin.getAdminGames() != null){
            String[] ids = admin.getAdminGames().split(",");
            Object[] result = new Object[ids.length];
            for(int i=0; i<ids.length;i++){
                result[i] = Integer.valueOf(ids[i]);
            }
            return result;
        }
        return new Object[]{};
    }


    public List<UAdmin> getAllAdmins(){

        return adminDao.findAll();
    }
}
