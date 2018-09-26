package com.u8.server.dao;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.UAdminRole;
import org.springframework.stereotype.Repository;

/**
 * 权限角色表数据操作
 * Created by ant on 2016/7/28.
 */
@Repository
public class UAdminRoleDao extends UHibernateTemplate<UAdminRole, Integer> {
}
