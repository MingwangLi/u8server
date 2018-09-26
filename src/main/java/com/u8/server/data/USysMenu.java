package com.u8.server.data;

import net.sf.json.JSONObject;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 系统功能表
 * Created by ant on 2016/7/28.
 */

@Entity
@Table(name = "usysmenu")
public class USysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;             //ID,唯一，主键

    private String name;            //名称
    private Integer parentID;       //父功能ID
    private String path;            //相对url
    private Date createTime;        //创建时间

    @Transient
    private List<USysMenu> children;    //子菜单

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("path", path);
        json.put("parentID", parentID);

//        if(children != null && children.size() > 0){
//            JSONArray array = new JSONArray();
//            for(USysMenu m : children){
//                array.add(m.toJSON());
//            }
//            json.put("children", array);
//        }

        return json;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getParentID() {
        return parentID;
    }

    public void setParentID(Integer parentID) {
        this.parentID = parentID;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<USysMenu> getChildren() {
        return children;
    }

    public void setChildren(List<USysMenu> children) {
        this.children = children;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
