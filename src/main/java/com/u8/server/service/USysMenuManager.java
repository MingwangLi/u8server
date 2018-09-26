package com.u8.server.service;

import com.u8.server.dao.USysMenuDao;
import com.u8.server.data.UAdminRole;
import com.u8.server.data.USysMenu;
import com.u8.server.utils.StringUtils;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统功能菜单逻辑管理类
 * Created by ant on 2016/7/28.
 */
@Service("sysMenuManager")
public class USysMenuManager {

    @Autowired
    private USysMenuDao sysMenuDao;

    
    public void saveSysMenu(USysMenu menu){
        if(menu.getParentID() == null) {
            menu.setParentID(0);
        }
        if(menu.getId() == null){
            menu.setCreateTime(new Date());
        }
        sysMenuDao.save(menu);
    }

    public List<USysMenu> getAllMenus(){

        return sysMenuDao.findAll();

    }

    public List<USysMenu> getRootMenus(){
        List<USysMenu> all = sysMenuDao.findAll();
        List<USysMenu> roots = new ArrayList<USysMenu>();

        for(USysMenu m : all){
            if(m.getParentID() <= 0){
                roots.add(m);
            }
        }

        return roots;

    }

    /**
     * 获取给定权限的所有权限
     * @param role
     * @return
     */
    public List<USysMenu> getMenusByPermission(UAdminRole role){

        if(role.getTopRole() != null && role.getTopRole().equals(1)){
            return getTreeMenus(getAllMenus());
        }

        List<String> menus = StringUtils.split2list(role.getPermission(), ",");
        List<USysMenu> matched = new ArrayList<USysMenu>();

        if(menus != null){
            List<USysMenu> all = getAllMenus();

            for(String mid : menus){
                for(USysMenu m : all){
                    if(m.getId().equals(Integer.valueOf(mid))){
                        matched.add(m);
                        break;
                    }
                }
            }

            return getTreeMenus(matched);
        }
        return matched;
    }

    /**
     * 获取给定权限的所有权限
     * @param permission
     * @return
     */
    public String getRealPermission(String permission){

        List<String> menus = StringUtils.split2list(permission, ",");

        if(menus != null){

            StringBuilder sb = new StringBuilder();

            List<USysMenu> all = getAllMenus();
            for(USysMenu m : all){
                for(String mid : menus){
                    if(m.getId().equals(Integer.valueOf(mid))
                            || m.getParentID().equals(Integer.valueOf(mid))){
                        sb.append(m.getId()).append(",");
                        break;
                    }
                }
            }

            return  sb.toString();
        }
        return "";
    }

    /**
     * 按照树状结构获取功能菜单，目前只支持最多二级菜单
     * @return
     */
    public List<USysMenu> getTreeMenus(){

        return getTreeMenus(sysMenuDao.findAll());
    }


    private List<USysMenu> getTreeMenus(List<USysMenu> all){

        List<USysMenu> roots = new ArrayList<USysMenu>();

        for(USysMenu m : all){
            if(m.getParentID() <= 0){
                roots.add(m);
            }
        }

        for(USysMenu m : roots){

            for(USysMenu n : all){

                if(n.getParentID().equals(m.getId())){
                    if(m.getChildren() == null){
                        m.setChildren(new ArrayList<USysMenu>());
                    }
                    m.getChildren().add(n);
                }
            }

        }

        return roots;
    }
}
