package com.u8.server.web.admin;

import com.opensymphony.xwork2.ModelDriven;
import com.u8.server.common.Page;
import com.u8.server.common.UActionSupport;
import com.u8.server.dao.UGameDao;
import com.u8.server.dao.UOrderDao;
import com.u8.server.data.*;
import com.u8.server.log.Log;
import com.u8.server.service.UAdminManager;
import com.u8.server.service.UGameManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * 游戏管理
 * Created by ant on 2015/8/26.
 */

@Controller
@Namespace("/admin/games")
public class GameAction extends UActionSupport implements ModelDriven<UGame> {


    private int page;           //当前请求的页码
    private int rows;           //当前每页显示的行数

    private UGame game;

    private int currAppID;
    private String admins;      //权限管理员ID，逗号分割

    @Autowired
    private UGameManager gameManager;

    @Autowired
    private UAdminManager adminManager;


    @Override
    public UGame getModel() {

        if (this.game == null) {
            this.game = new UGame();
        }

        return this.game;
    }


    @Action(value = "showGames", results = {@Result(name = "success", location = "/WEB-INF/admin/games.jsp")})
    public String channelManage() {

        return "success";
    }

    @Action("getAllGames")
    public void getAllGames() {
        try {
            UAdmin admin = (UAdmin) session.get("admin");
            Page<UGame> currPage = gameManager.queryPage(page, rows, game.getAppID(), game.getAppkey(), game.getAppSecret(), game.getName(), adminManager.getPermissonedGameIDs(admin));
            JSONObject json = new JSONObject();

            json.put("total", currPage == null ? 0 : currPage.getTotalCount());
            JSONArray users = new JSONArray();
            if (currPage != null) {
                for (UGame m : currPage.getResultList()) {
                    users.add(m.toJSON());
                }
            }

            json.put("rows", users);

            renderJson(json.toString());

//            int count = this.gameManager.getGameCount();
//
//            List<UGame> lst = this.gameManager.queryPage(page, rows);
//
//            JSONObject json = new JSONObject();
//            json.put("total", count);
//            JSONArray masterArray = new JSONArray();
//            if(lst != null){
//                for(UGame m : lst){
//                    masterArray.add(m.toJSON());
//                }
//            }
//
//            json.put("rows", masterArray);
//
//            renderJson(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Action("getAllGamesSimple")
    public void getAllGamesSimple() {
        try {
            UAdmin admin = (UAdmin) session.get("admin");
            List<UGame> games = this.gameManager.queryAllGames(adminManager.getPermissonedGameIDs(admin));

            JSONArray gameArray = new JSONArray();
            for (UGame game : games) {
                JSONObject item = new JSONObject();
                item.put("appID", game.getAppID());
                item.put("name", game.getName());
                gameArray.add(item);
            }

            renderJson(gameArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //添加或者编辑
    @Action("saveGame")
    public void saveChannelMasters() {

        try {

            Log.d("save.game.info." + this.game.toJSON().toString());

            UGame currGame;
            if (this.game.getAppID() != null && this.game.getAppID() > 0) {
                //编辑 修改游戏相关数据
                currGame = gameManager.queryGame(this.game.getAppID());
                if (currGame != null) {
                    currGame.setName(this.game.getName());
                    currGame.setPayCallback(this.game.getPayCallback());
                    currGame.setPayCallbackDebug(this.game.getPayCallbackDebug());
                    currGame.setMsdkPayCallback(this.game.getMsdkPayCallback());
                    gameManager.saveGame(currGame);
                }
            } else {
                //新增 保存游戏相关数据和游戏权限
                currGame = this.gameManager.generateGame(this.game.getName(), this.game.getPayCallback(), this.game.getPayCallbackDebug(), this.game.getMsdkPayCallback());
                UAdmin admin = (UAdmin) session.get("admin");
                adminManager.addAdminGamePermission(admin, currGame.getAppID());
            }
            //绑定游戏权限
            if (currGame != null && currGame.getAppID() != null && this.admins != null) {

                String[] adminIDs = this.admins.split(",");
                for (String adminID : adminIDs) {
                    if (adminID.trim().length() > 0) {
                        int id = Integer.valueOf(adminID.trim());
                        UAdmin admin = adminManager.getAdmin(id);
                        if (admin != null) {
                            adminManager.addAdminGamePermission(admin, currGame.getAppID());
                        }
                    }

                }

            }
            renderState(true);
            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }

    @Action("removeGame")
    public void removeGame() {
        try {

            Log.d("Curr gameID is " + this.currAppID);

            UGame m = this.gameManager.queryGame(this.currAppID);
            if (m == null) {
                renderState(false);
                return;
            }

            List<UChannel> lst = this.gameManager.queryChannels(this.currAppID);
            if (lst.size() > 0) {
                renderState(false, "请先删除该游戏下面的所有渠道数据");
                return;
            }

            this.gameManager.deleteGame(m);

            renderState(true);
            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderState(false);
    }

    @Autowired
    private UGameDao UGameDao;



    @Action("/editGame")
    public void editGame(){
        try {
            UGame ugame = UGameDao.get(game.getAppID());
            if (game.getAppID() != 1) {
                renderState(false,"不允许修改的数据");
                return;
            }
            ugame.setName(game.getName());
            ugame.setAppkey(game.getAppkey());
            ugame.setAppSecret(game.getAppSecret());
            ugame.setAppRSAPriKey(game.getAppRSAPriKey());
            ugame.setAppRSAPubKey(game.getAppRSAPubKey());
            ugame.setMsdkPayCallback(game.getMsdkPayCallback());
            ugame.setPayCallback(game.getPayCallback());
            ugame.setPayCallbackDebug(game.getPayCallbackDebug());
            gameManager.saveGame(ugame);
            renderState(true);
        } catch (Exception e) {
            e.printStackTrace();
            renderState(false,e.getMessage());
        }
    }


    private void renderState(boolean suc) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("msg", suc ? "操作成功" : "操作失败");
        renderText(json.toString());
    }

    private void renderState(boolean suc, String msg) {
        JSONObject json = new JSONObject();
        json.put("state", suc ? 1 : 0);
        json.put("msg", msg);
        renderText(json.toString());
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public UGame getGame() {
        return game;
    }

    public void setGame(UGame game) {
        this.game = game;
    }

    public int getCurrAppID() {
        return currAppID;
    }

    public void setCurrAppID(int currAppID) {
        this.currAppID = currAppID;
    }

    public String getAdmins() {
        return admins;
    }

    public void setAdmins(String admins) {
        this.admins = admins;
    }
}
