package com.u8.server.service;

import com.u8.server.cache.CacheManager;
import com.u8.server.common.Page;
import com.u8.server.common.PageParameter;
import com.u8.server.common.SQLParams;
import com.u8.server.dao.UGameDao;
import com.u8.server.data.UChannel;
import com.u8.server.data.UGame;
import com.u8.server.utils.IDGenerator;
import com.u8.server.utils.RSAUtils;
import com.u8.server.utils.StringUtils;
import com.u8.server.utils.UGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service("gameManager")
public class UGameManager {

    @Autowired
    private UGameDao gameDao;

    
    public UGame generateGame(String name, String payCallback, String payCallbackDebug,String msdkCallback){

        UGame game = new UGame();
        int appID = IDGenerator.getInstance().nextAppID();
        long currTime = System.currentTimeMillis();
        game.setAppID(appID);
        game.setAppkey(UGenerator.generateAppKey(appID, currTime));
        game.setAppSecret(UGenerator.generateAppSecret());
        game.setName(name);
        game.setPayCallbackDebug(payCallback);
        game.setPayCallback(payCallbackDebug);
        game.setMsdkPayCallback(msdkCallback);

        try {
            Map<String, Object> keys = RSAUtils.generateKeys();
            game.setAppRSAPubKey(RSAUtils.getPublicKey(keys));
            game.setAppRSAPriKey(RSAUtils.getPrivateKey(keys));
        } catch (Exception e) {
            e.printStackTrace();
        }

        CacheManager.getInstance().addGame(game);
        gameDao.save(game);

        return game;
    }


    
    public void saveGame(UGame game){
        CacheManager.getInstance().saveGame(game);
        gameDao.save(game);
    }

    
    public void deleteGame(UGame game){

        if(game == null){
            return;
        }

        CacheManager.getInstance().removeGame(game.getAppID());
        gameDao.delete(game);

    }

    public UGame queryGame(int appID){

        return CacheManager.getInstance().getGame(appID);
    }

    public UGame getGameByName(String name){

        return CacheManager.getInstance().getGameByName(name);
    }


    public String getGameByNameList(String name) {
        return CacheManager.getInstance().getGameByNameList(name);
    }

    public List<UGame> queryAllGames(Object[] permissionedGameIDs){

        List<UGame> games = CacheManager.getInstance().getGameList();
        if(permissionedGameIDs == null){
            return games;
        }

        if(permissionedGameIDs.length == 0){
            return new ArrayList<UGame>();
        }

        List<UGame> result = new ArrayList<UGame>();
        for(UGame game : games){
            for(Object p : permissionedGameIDs){
                if(game.getAppID().equals(p)){
                    result.add(game);
                    break;
                }
            }
        }
        return result;
    }

    public int getGameCount(){

        return CacheManager.getInstance().getGameList().size();
    }

    public List<UChannel> queryChannels(int appID){
        List<UChannel> lst = CacheManager.getInstance().getChannelList();

        List<UChannel> result = new ArrayList<UChannel>();
        for(UChannel c : lst){
            if(c.getAppID() == appID){
                result.add(c);
            }
        }

        return result;
    }

    //分页查找
    public List<UGame> queryPage(int currPage, int num){

        List<UGame> masters = CacheManager.getInstance().getGameList();

        Collections.sort(masters, new Comparator<UGame>() {
            @Override
            public int compare(UGame o1, UGame o2) {
                return o1.getAppID() - o2.getAppID();
            }
        });

        int fromIndex = (currPage-1) * num;

        if(fromIndex >= masters.size()){

            return null;
        }

        int endIndex = Math.min(fromIndex+num, masters.size());

        return masters.subList(fromIndex, endIndex);
    }

    //分页查找， 直接从数据库中查找
    public Page<UGame> queryPage(int currPage, int num, Integer appID, String appKey, String appSecret, String name, Object[] permissionedGameIDs){

        SQLParams params = new SQLParams();
        if(appID != null && appID > 0){
            params.EQ("appID", appID);
        }

        if(!StringUtils.isEmpty(appKey)){
            params.EQ("appkey", appKey);
        }

        if(!StringUtils.isEmpty(appSecret)){
            params.EQ("appSecret", appSecret);
        }

        if(!StringUtils.isEmpty(name)){
            params.EQ("name", name);
        }

        if(permissionedGameIDs != null){
            if(permissionedGameIDs.length == 0){
                permissionedGameIDs = new Object[]{0};
            }
            params.IN("appID", permissionedGameIDs);
        }

        PageParameter page = new PageParameter(currPage, num, true);
        String hql = "from UGame" + params.getWhereSQL();

        return gameDao.find(page, hql, params.getWhereValues(), null);
    }


    
    public void testTransaction(Integer appID) {
            UGame uGame = gameDao.get(appID);
            uGame.setName("测试");
            gameDao.save(uGame);
    }

    public List<UGame> getAllGames() {
        return gameDao.findAll();
    }
}
