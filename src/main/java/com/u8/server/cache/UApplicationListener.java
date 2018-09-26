package com.u8.server.cache;

import com.u8.server.dao.UChannelDao;
import com.u8.server.dao.UChannelMasterDao;
import com.u8.server.dao.UGameDao;
import com.u8.server.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 在Spring启动的时候，加载需要缓存的数据
 */
@Repository
@Transactional(readOnly = true)
public class UApplicationListener implements ApplicationListener<ApplicationEvent>{

    @Autowired
    private UGameDao gameDao;
    @Autowired
    private UChannelDao channelDao;
    @Autowired
    private UChannelMasterDao channelMasterDao;

    private static AtomicBoolean loaded = new AtomicBoolean(false);

    /**
     * 此方法在多线程下访问可能会出现问题
     * 这里使用AtomicBoolean 保证loaded变量读写同步 该方法只执行一次
     * @param contextStartedEvent
     */
    @Override
    public void onApplicationEvent(ApplicationEvent contextStartedEvent) {
        try{
            if (loaded.compareAndSet(false,true)) {
                CacheManager.getInstance().loadGameData(gameDao.findAll());
                CacheManager.getInstance().loadMasterData(channelMasterDao.findAll());
                CacheManager.getInstance().loadChannelData(channelDao.findAll());
            }
           /* if(!loaded){

                Log.e("Spring now to load...");

                CacheManager.getInstance().loadGameData(gameDao.findAll());
                CacheManager.getInstance().loadMasterData(channelMasterDao.findAll());
                CacheManager.getInstance().loadChannelData(channelDao.findAll());
                //MailUtils.getInstance().sendMail("3462951792@qq.com","测试邮件","这是一封服务启动的测试邮件,如果您收到此邮件,则表明邮件服务系统OK Thanks!");
                loaded = true;
            }*/

        }catch (Exception e){
            Log.e("Load Data on server inited error.", e);
        }
    }
}
