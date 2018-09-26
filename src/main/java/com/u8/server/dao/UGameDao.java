package com.u8.server.dao;

import com.u8.server.common.UHibernateTemplate;
import com.u8.server.data.UGame;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 游戏对象数据访问类
 */
@Repository
public class UGameDao extends UHibernateTemplate<UGame, Integer>{


    public void saveGame(UGame game){
        super.save(game);
    }

    public UGame queryGame(int appID){return super.get(appID);}

    public UGame findByName(String name) {
        String hql = "from UGame where name = ?";
        Query query = this.getSession().createQuery(hql);
        query.setParameter(0,name);
        //不适合模糊查询
        //String hql = "from UGame where name like :name";
        //query.setString("name","%"+name+"%");
        List<UGame> list = query.list();
        if (null != list) {
            return list.get(0);
        }
        return null;
    }
}
