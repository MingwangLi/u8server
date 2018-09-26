package com.u8.server.dao;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.USysMenu;
import org.springframework.stereotype.Repository;

/**
 * 系统功能菜单操作类
 * Created by ant on 2016/7/28.
 */
@Repository
public class USysMenuDao extends UHibernateTemplate<USysMenu, Integer> {

}
