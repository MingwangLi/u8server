package com.u8.server.web.admin;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.UAdmin;
import com.u8.server.data.UAdminRole;
import com.u8.server.data.USysMenu;
import com.u8.server.log.Log;
import com.u8.server.service.UAdminRoleManager;
import com.u8.server.service.USysMenuManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.util.TextUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * 权限角色操作
 * Created by ant on 2016/7/30.
 */
@Controller
@Namespace("/admin")
public class AdminRoleAction extends UActionSupport implements ModelDriven<UAdminRole> {

    private UAdminRole role;

    private Integer adminRoleID;
    private String rolePermission;

    @Autowired
    private UAdminRoleManager adminRoleManager;

    @Autowired
    private USysMenuManager sysMenuManager;

    @Action("getAllAdminRoles")
    public void getAllAdminRoles() {
        try {

            List<UAdminRole> lst = adminRoleManager.getAdminRoles();

            JSONArray array = new JSONArray();
            for (UAdminRole m : lst) {
                array.add(m.toJSON());
            }

            renderJson(array.toString());
            System.out.println(array.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("updateRolePermission")
    public void updateRolePermission() {
        try {

            UAdminRole role = adminRoleManager.getAdminRoleByID(this.adminRoleID);
            if (role != null) {

                if (TextUtils.isEmpty(this.rolePermission)) {
                    role.setPermission("");
                } else {
                    //role.setPermission(sysMenuManager.getRealPermission(this.rolePermission));
                    role.setPermission(this.rolePermission.substring(0,this.rolePermission.length()-1));
                }

                Log.d("the role is" + role.toJSON().toString());
                Log.d("the permision is " + this.rolePermission);

                adminRoleManager.saveAdminRole(role);
                renderState(true);
            }


            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }

    @Action("saveAdminRole")
    public void saveAdminRole() {
        try {
            Log.d("save.adminRole.info." + this.role.toJSON().toString());

            if (role.getId() == null) {
                UAdmin loginedAdmin = (UAdmin) session.get("admin");
                if (loginedAdmin != null) {
                    role.setCreatorID(loginedAdmin.getId());
                }
            }

            adminRoleManager.saveAdminRole(this.role);
            renderState(true);

            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }

    @Action("removeAdminRole")
    public void removeAdminRole() {
        try {
            Log.d("Curr roleID is " + this.role.getId());

            UAdminRole currRole = adminRoleManager.getAdminRoleByID(this.role.getId());

            if (currRole == null) {
                renderState(false);
                return;
            }

            adminRoleManager.deleteAdminRole(currRole);

            renderState(true);

            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }


    private void renderState(boolean suc) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("msg", suc ? "操作成功" : "操作失败");
        renderText(json.toString());
    }


    @Override
    public UAdminRole getModel() {
        if (this.role == null) {
            this.role = new UAdminRole();
        }

        return this.role;
    }

    public UAdminRole getRole() {
        return role;
    }

    public void setRole(UAdminRole role) {
        this.role = role;
    }

    public Integer getAdminRoleID() {
        return adminRoleID;
    }

    public void setAdminRoleID(Integer adminRoleID) {
        this.adminRoleID = adminRoleID;
    }

    public String getRolePermission() {
        return rolePermission;
    }

    public void setRolePermission(String rolePermission) {
        this.rolePermission = rolePermission;
    }
}
