package com.u8.server.service;

import com.u8.server.dao.UAdminRoleDao;
import com.u8.server.data.UAdminRole;
import com.u8.server.data.USysMenu;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.Role;
import java.util.Date;
import java.util.List;

/**
 * 权限角色逻辑管理类
 * Created by ant on 2016/7/28.
 */
@Service("adminRoleManager")
public class UAdminRoleManager {

    @Autowired
    private UAdminRoleDao adminRoleDao;


    public void saveAdminRole(UAdminRole role){
        if(role.getId() == null){
            role.setCreateTime(new Date());
        }
        if(role.getPermission() == null){
            role.setPermission("");
        }

        adminRoleDao.save(role);
    }

    //指定角色是否有对应的功能权限
    public boolean hasPermission(UAdminRole role, USysMenu menu){

        if(role == null || menu == null){
            return  false;
        }

        String[] menus = null;
        if(!TextUtils.isEmpty(role.getPermission())){
            menus = role.getPermission().split(",");
        }

        if(menus != null){
            for(String mid : menus){
                if(menu.getId().equals(Integer.valueOf(mid))){
                    return true;
                }
            }
        }

        return  false;
    }

    public List<UAdminRole> getAdminRoles(){

        return adminRoleDao.findAll();
    }

    public UAdminRole getAdminRoleByID(Integer id){
        if(id == null) return null;
        return adminRoleDao.get(id);
    }



    public void deleteAdminRole(UAdminRole role){
        adminRoleDao.delete(role);
    }
}
