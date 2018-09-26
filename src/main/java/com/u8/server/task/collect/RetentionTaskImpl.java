package com.u8.server.task.collect;

import com.u8.server.dao.analytics.TRetentionDao;
import com.u8.server.service.UGameManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



@Component
public class RetentionTaskImpl implements RetentionTask{

    private Logger logger = LoggerFactory.getLogger(RetentionTaskImpl.class);

    @Autowired
    private UGameManager uGameManager;

    @Autowired
    private TRetentionDao tRetentionDao;

    //@Scheduled(cron = "0 0 1 * * ?")
    @Override
    public void addDayRetention() {
        /*List<UGame> list = uGameManager.getAllGames();
        for (UGame uGame:list) {
            int appID = uGame.getAppID();
            tRetentionDao.addDayRetention(appID);
        }*/
    }

    //@Scheduled(cron = "0 30 1 * * ?")
    @Override
    public void collectDayRetention() {
       /* List<UGame> list = uGameManager.getAllGames();
        for (UGame uGame:list) {
            int appID = uGame.getAppID();
            tRetentionDao.collectDayRetention(appID);
        }*/
    }
}
