package com.u8.server.cache;

import com.u8.server.data.UChannel;
import com.u8.server.data.UChannelMaster;
import com.u8.server.data.UGame;
import com.u8.server.log.Log;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 将常用的数据进行缓存。包含game,master,channel等对象
 */
public class CacheManager {

    private static CacheManager instance;

    private Map<Integer, UGame> games;
    private Map<Integer, UChannelMaster> masters;
    private Map<Integer, UChannel> channels;

    private CacheManager(){

    }

    public synchronized static CacheManager getInstance(){
        if(instance == null){
            instance = new CacheManager();
        }
        return instance;
    }

    public List<UGame> getGameList(){

        return new ArrayList<UGame>(games.values());
    }

    public List<UChannel> getChannelList(){

        return new ArrayList<UChannel>(channels.values());
    }

    public List<UChannelMaster> getMasterList(){

        return new ArrayList<UChannelMaster>(masters.values());
    }

    public UGame getGame(int appID){
        if(this.games.containsKey(appID)){
            return this.games.get(appID);
        }
        return null;
    }

    public UGame getGameByName(String name){
        for(UGame g : this.games.values()){
            if(g.getName() != null && g.getName().equalsIgnoreCase(name)){
                return g;
            }
        }

        return null;
    }

    public String getGameByNameList(String name) {
        String gameIDs = "";
        for(UGame g : this.games.values()){
            if(g.getName() != null && g.getName().contains(name)){
                gameIDs=gameIDs+g.getAppID()+",";
            }
        }
        if (StringUtils.isNotEmpty(gameIDs)) {
            gameIDs=gameIDs.substring(0,gameIDs.lastIndexOf(","));
        }

        return gameIDs;
    }

    public UChannelMaster getMaster(int masterID){
        if(this.masters.containsKey(masterID)){
            return this.masters.get(masterID);
        }
        return null;
    }

    public UChannelMaster getMasterByName(String name){
        for(UChannelMaster m : this.masters.values()){
            if(m.getMasterName() != null && m.getMasterName().contains(name)){
                return m;
            }
        }
        return null;
    }

    public String getMasterByNameList(String name) {
        String masterIDs = "";
        for(UChannelMaster m : this.masters.values()){
            if(m.getMasterName() != null && m.getMasterName().contains(name)){
               masterIDs = masterIDs+(m.getMasterID()+",");
            }
        }
        if (StringUtils.isNotEmpty(masterIDs)) {
            masterIDs = masterIDs.substring(0,masterIDs.lastIndexOf(","));
        }

        return masterIDs;
    }

    public UChannel getChannel(int channelID){
        if(this.channels.containsKey(channelID)){
            return this.channels.get(channelID);
        }
        return null;
    }

    public UChannel getChannelByID(Integer id){

        if(id == null){
            return null;
        }

        for(UChannel c : this.channels.values()){
            if(c.getId().equals(id)){
                return c;
            }
        }
        return null;
    }

    public void addGame(UGame game){

        if(games.containsKey(game.getAppID())){
            Log.e("The appID is already is exists. add game failed."+game.getAppID());
            return;
        }

        games.put(game.getAppID(), game);

    }

    public void saveGame(UGame game){

        if(games.containsKey(game.getAppID())){
            games.remove(game.getAppID());
        }
        games.put(game.getAppID(), game);

    }

    public void addMaster(UChannelMaster master){

        if(masters.containsKey(master.getMasterID())){
            Log.e("The channel master ID is already is exists. add channel master faild."+master.getMasterID());
            return;
        }

        masters.put(master.getMasterID(), master);

    }

    public void saveMaster(UChannelMaster master){

        if(masters.containsKey(master.getMasterID())){
            masters.remove(master.getMasterID());
        }
        masters.put(master.getMasterID(), master);

    }

    public void removeMaster(int masterID){

        if(masters.containsKey(masterID)){
            masters.remove(masterID);
        }

    }

    public void addChannel(UChannel channel){

        if(channels.containsKey(channel.getChannelID())){
            Log.e("The channelID is already is exists. add channel faild."+channel.getChannelID());
            return;
        }

        channels.put(channel.getChannelID(), channel);


    }

    //添加或者修改渠道
    public void saveChannel(UChannel channel){

        if(channels.containsKey(channel.getChannelID())){
            channels.remove(channel.getChannelID());
        }

        Log.d("the channel is "+channel);
        UChannel c = getChannelByID(channel.getId());
        if(c != null){
            channels.remove(c.getChannelID());

        }

        channels.put(channel.getChannelID(), channel);


    }

    public void removeChannel(int channelID){

        if(channels.containsKey(channelID)){
            channels.remove(channelID);
        }

    }

    public void removeGame(int appID){

        if(games.containsKey(appID)){
            games.remove(appID);
        }


    }

    public void loadGameData(List<UGame> gameLst){
        games = new HashMap<Integer, UGame>();
        for(UGame game : gameLst){
            games.put(game.getAppID(), game);
        }
        Log.i("Load games :"+ games.size());
    }

    public void loadMasterData(List<UChannelMaster> masterLst){
        masters = new HashMap<Integer, UChannelMaster>();

        for(UChannelMaster master : masterLst){
            masters.put(master.getMasterID(), master);
        }
        Log.i("Load masters:"+ masters.size());
    }

    public void loadChannelData(List<UChannel> channelLst){
        channels = new HashMap<Integer, UChannel>();
        for(UChannel channel : channelLst){
            channels.put(channel.getChannelID(), channel);
        }
        Log.i("Load channels:"+channels.size());
    }

    public Map<Integer, UChannelMaster> getMasters() {
        return masters;
    }

    public void setMasters(Map<Integer, UChannelMaster> masters) {
        this.masters = masters;
    }

    public Map<Integer, UGame> getGames() {
        return games;
    }

    public void setGames(Map<Integer, UGame> games) {
        this.games = games;
    }

    public Map<Integer, UChannel> getChannels() {
        return channels;
    }

    public void setChannels(Map<Integer, UChannel> channels) {
        this.channels = channels;
    }
}
