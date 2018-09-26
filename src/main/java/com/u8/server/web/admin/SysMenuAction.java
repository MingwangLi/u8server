package com.u8.server.web.admin;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.UAdmin;
import com.u8.server.data.UAdminRole;
import com.u8.server.data.UChannel;
import com.u8.server.data.USysMenu;
import com.u8.server.log.Log;
import com.u8.server.service.UAdminManager;
import com.u8.server.service.UAdminRoleManager;
import com.u8.server.service.USysMenuManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.util.TextUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统功能菜单Action
 * Created by ant on 2016/7/28.
 */
@Controller
@Namespace("/admin")
public class SysMenuAction extends UActionSupport implements ModelDriven<USysMenu> {

    private USysMenu menu;

    private Integer adminRoleID;


    @Autowired
    private USysMenuManager sysMenuManager;

    @Autowired
    private UAdminRoleManager adminRoleManager;

    @Action("getAllMenus")
    public void getAllMenus() {
        try {

            UAdminRole role = adminRoleManager.getAdminRoleByID(adminRoleID);

            List<USysMenu> lst = sysMenuManager.getTreeMenus();

            JSONArray array = new JSONArray();
            for (USysMenu m : lst) {

                JSONObject json = m.toJSON();
                json.put("isChecked", adminRoleManager.hasPermission(role, m));

                if (m.getChildren() != null) {
                    JSONArray children = new JSONArray();
                    for (USysMenu c : m.getChildren()) {
                        JSONObject cj = c.toJSON();
                        cj.put("isChecked", adminRoleManager.hasPermission(role, c));
                        children.add(cj);
                    }
                    json.put("children", children);
                }

                array.add(json);

            }
            renderJson(array.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Action("saveSysMenu")
    public void saveSysMenu() {
        try {
            Log.d("save.SysMenu.info." + this.menu.toJSON().toString());

            sysMenuManager.saveSysMenu(this.menu);
            renderState(true);

            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }

    @Action("getAllRootMenus")
    public void getAllRootMenus() {
        try {

            List<USysMenu> lst = sysMenuManager.getRootMenus();
            JSONArray array = new JSONArray();
            for (USysMenu m : lst) {
                JSONObject json = m.toJSON();
                json.put("showName", m.getName() + "(" + m.getId() + ")");
                array.add(json);
            }

            renderJson(array.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderState(boolean suc) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("msg", suc ? "操作成功" : "操作失败");
        renderText(json.toString());
    }

    @Override
    public USysMenu getModel() {
        if (this.menu == null) {
            this.menu = new USysMenu();
        }

        return this.menu;
    }

    public Integer getAdminRoleID() {
        return adminRoleID;
    }

    public void setAdminRoleID(Integer adminRoleID) {
        this.adminRoleID = adminRoleID;
    }

    public USysMenu getMenu() {
        return menu;
    }

    public void setMenu(USysMenu menu) {
        this.menu = menu;
    }
}
