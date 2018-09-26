package com.u8.server.web.test;

import com.u8.server.common.UActionSupport;
import com.u8.server.dao.UUserDao;
import com.u8.server.data.UUser;
import com.u8.server.service.UUserManager;
import org.apache.struts2.convention.annotation.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/admin")
public class TransactionTestController extends UActionSupport {

    @Autowired
    private UUserDao uUserDao;

    @Autowired
    private UUserManager uUserManager;

    @Action("/test")
    public void testTransaction() {
        UUser user = uUserDao.get(33599);
        user.setToken("why");
        //uUserDao.save(user);     //no error ,not update the data of database  why?  Controller是通过struts2实例化并放入容器的  而Service Dao是通过Spring实例化并放入容器的  aop是基于Spring的IOC和DI 当然无法更新
        uUserManager.saveUser(user);   //ok     uUserManager跟uUserDao都是在Spring容器中 当然可以生成uUserDao的代理对象执行保存操作并执行aop(事务)
    }


}
