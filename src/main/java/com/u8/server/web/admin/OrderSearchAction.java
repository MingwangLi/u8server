package com.u8.server.web.admin;

import com.u8.server.enums.OrderStateEnum;
import com.u8.server.cache.CacheManager;
import com.u8.server.common.Page;
import com.u8.server.common.UActionSupport;
import com.u8.server.data.UAdmin;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.service.UAdminManager;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.DownloadUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单查询
 * Created by ant on 2016/8/5.
 */
@Namespace("/admin/orders")
public class OrderSearchAction extends UActionSupport {

    private int page;               //当前请求的页码
    private int rows;               //当前每页显示的行数

    private Long orderID;        //订单号
    private Integer appID;          //当前所属游戏ID
    private Integer channelID;      //当前所属渠道ID
    private Integer userID;         //U8Server这边对应的用户ID
    private String username;       //U8Server这边生成的用户名
    private String productID;      //游戏中商品ID
    private String productName;    //游戏中商品名称

    private Integer minMoney;       //单位 分, 下单时收到的金额，实际充值的金额以这个为准
    private Integer maxMoney;
    private Integer minRealMoney;   //单位 分，渠道SDK支付回调通知返回的金额，记录，留作查账
    private Integer maxRealMoney;

    private String roleID;         //游戏中角色ID
    private String roleName;       //游戏中角色名称
    private String serverID;       //服务器ID
    private Integer state;          //订单状态
    private String channelOrderID;  //渠道SDK对应的订单号

    private Date beginCreateTime;       //订单创建时间
    private Date endCreateTime;


    @Autowired
    private UOrderManager orderManager;

    @Autowired
    private UAdminManager adminManager;

    @Action("searchOrders")
    public void searchOrders() {
        try {
            UAdmin admin = (UAdmin) session.get("admin");
            Page<UOrder> currPage = this.orderManager.queryPage(page, rows, orderID, appID, channelID, userID, username, productID
                    , productName, minMoney, maxMoney, minRealMoney, maxRealMoney, roleID, roleName, serverID, state
                    , channelOrderID, beginCreateTime, endCreateTime, adminManager.getPermissonedGameIDs(admin));

            JSONObject json = new JSONObject();
            json.put("total", currPage == null ? 0 : currPage.getTotalCount());
            JSONArray users = new JSONArray();
            if (currPage != null) {
                for (UOrder m : currPage.getResultList()) {
                    users.add(m.toJSON());
                }
            }

            json.put("rows", users);

            renderJson(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Integer allgames;

    public void setAllgames(Integer allgames) {
        this.allgames = allgames;
    }

    /**
     * 根据搜索出来的条件订单下载
     * 优化：
     * 1.放弃使用流读取模板  居中 样式等下载完成自己打开调整 保证响应速度
     * 2.使用SXSSFWorkbook  之前使用XSSFWorkbook 7000记录 5min  使用SXSSFWorkbook 45000记录 2min
     */
    @SuppressWarnings("all ")
    @Action("downloadOrdersWithConditions")
    public void downloadOrdersWithConditions() {
        OutputStream os = null;
        try{
            os = this.response.getOutputStream();
            //InputStream is = this.getClass().getClassLoader().getResourceAsStream("订单报表模板.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook();
            SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
            List<UOrder> list = orderManager.download(allgames, channelID, state,beginCreateTime, endCreateTime);
            Integer maxRow = 1048576;
            if(list.size() >= maxRow) {
                Log.d("----下载订单失败,数据量过大");
                String content = "下载订单失败,数据量过大,请重新筛选";
                os.write(content.getBytes("UTF-8"));
                return;
            }
            Sheet sheet = sxssfWorkbook.createSheet();
            Row first = sheet.createRow(0);
            Row secend = sheet.createRow(1);
            List<String> title = new ArrayList<>();
            title.add("订单号");
            title.add("用户名");
            title.add("金额(分)");
            title.add("实际金额");
            title.add("状态");
            title.add("渠道订单号");
            title.add("渠道号");
            title.add("渠道名称");
            title.add("所属游戏");
            title.add("下单时间");
            for (int m=0;m<10;m++) {
                first.createCell(m);
                Cell cell = secend.createCell(m);
                cell.setCellValue(title.get(m));
            }
            sheet.addMergedRegion(new CellRangeAddress(0,0,0,9));
            Cell mergeCell = first.getCell(0);
            mergeCell.setCellValue("订单信息");
            title = null;
            Row row;
            Cell cell;
            for(int i = 2;i < list.size()+2;i++) {
                row = sheet.createRow(i);
                UOrder uOrder = list.get(i-2);
                List<String> values = new ArrayList<>();
                values.add(uOrder.getOrderID()+"");
                values.add(uOrder.getUsername());
                values.add(uOrder.getMoney()+"");
                Integer realMoney = uOrder.getRealMoney();
                values.add((realMoney==null?0:realMoney)+"");
                String state = null;
                for (OrderStateEnum orderStateEnum:OrderStateEnum.values()) {
                    if (orderStateEnum.getCode() == uOrder.getState()) {
                        state = orderStateEnum.getValue();
                    }
                }
                values.add(state);
                values.add(uOrder.getChannelOrderID());
                values.add(uOrder.getChannelID()+"");
                Integer masterId = CacheManager.getInstance().getChannel(uOrder.getChannelID()).getMasterID();
                String masterName = CacheManager.getInstance().getMaster(masterId).getMasterName();
                values.add(masterName);
                String gameName = CacheManager.getInstance().getGame(uOrder.getAppID()).getName();
                values.add(gameName);
                values.add(uOrder.getCreatedTime()+"");
                for (int j = 0;j < 10;j++) {
                    cell = row.createCell(j);
                    cell.setCellValue(values.get(j));
                }
                values.clear();
            }
            list.clear();
            String fileName = "订单报表.xlsx";
            DownloadUtil downloadUtil = new DownloadUtil();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            sxssfWorkbook.write(outputStream);
            downloadUtil.download(outputStream,this.response,fileName);

        }catch(Exception e) {
            Log.e("-----下载订单失败:"+e.getMessage());
           return ;
        }
    }

    /**
     * 下载每天已完成的订单
     */
    @SuppressWarnings("all ")
    @Action("downloadOrders")
    public void downloadOrders() {
        try {
            //URL url = this.getClass().getClassLoader().getResource("每日订单报表模板.xlsx");
            //Log.i("订单模板url"+url.toString());
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("订单报表模板.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(date);
            XSSFRow row = sheet.getRow(0);
            XSSFCell cell = row.getCell(0);
            cell.setCellValue(today+"日订单报表");
            cell.getCellStyle().setAlignment(XSSFCellStyle.ALIGN_CENTER_SELECTION);
            row = sheet.getRow(1);
            List<XSSFCellStyle> cellStyleList = new ArrayList<>();
            for(int i = 0;i < 10;i++) {
                cell = row.getCell(i);
                XSSFCellStyle cellStyle = cell.getCellStyle();
                cellStyleList.add(cellStyle);
            }
            is.close();
            //查询数据
            List<UOrder> list = orderManager.getTodayOrders(new Date());
            if (null == list) {
                Log.i("今日没有订单");
                ServletActionContext.getResponse().getWriter().print("今日没有订单");
                ServletActionContext.getResponse().getWriter();
                return;
            }
            for(int i = 2;i < list.size()+2;i++) {
                row = sheet.createRow(i);
                UOrder uOrder = list.get(i-2);
                List<String> values = new ArrayList<>();
                values.add(uOrder.getOrderID()+"");
                values.add(uOrder.getUsername());
                values.add(uOrder.getMoney()+"");
                Integer realMoney = uOrder.getRealMoney();
                values.add((realMoney==null?0:realMoney)+"");
                values.add(uOrder.getState()+"");
                values.add(uOrder.getChannelOrderID());
                values.add(uOrder.getChannelID()+"");
                Integer masterId = CacheManager.getInstance().getChannel(uOrder.getChannelID()).getMasterID();
                String masterName = CacheManager.getInstance().getMaster(masterId).getMasterName();
                values.add(masterName);
                String gameName = CacheManager.getInstance().getGame(uOrder.getAppID()).getName();
                values.add(gameName);
                values.add(uOrder.getCreatedTime()+"");
                for (int j = 0;j < 10;j++) {
                    cell = row.createCell(j);
                    XSSFCellStyle style = cellStyleList.get(j);
                    XSSFCellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setBorderBottom( style.getBorderBottom());
                    cellStyle.setBorderLeft(style.getBorderLeft());
                    cellStyle.setBorderTop(style.getBorderTop());
                    cellStyle.setBorderRight(style.getBorderRight());
                    cell.setCellStyle(cellStyle);
                    cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
                    cell.setCellValue(values.get(j));
                }

            }
            String fileName = today+"日订单报表.xlsx";
            DownloadUtil downloadUtil = new DownloadUtil();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            downloadUtil.download(outputStream,this.response,fileName);

        } catch (Exception e) {
            Log.e(e.getMessage());
            return ;
        }

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

    public Long getOrderID() {
        return orderID;
    }

    public void setOrderID(Long orderID) {
        this.orderID = orderID;
    }

    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getMinMoney() {
        return minMoney;
    }

    public void setMinMoney(Integer minMoney) {
        this.minMoney = minMoney;
    }

    public Integer getMaxMoney() {
        return maxMoney;
    }

    public void setMaxMoney(Integer maxMoney) {
        this.maxMoney = maxMoney;
    }

    public Integer getMinRealMoney() {
        return minRealMoney;
    }

    public void setMinRealMoney(Integer minRealMoney) {
        this.minRealMoney = minRealMoney;
    }

    public Integer getMaxRealMoney() {
        return maxRealMoney;
    }

    public void setMaxRealMoney(Integer maxRealMoney) {
        this.maxRealMoney = maxRealMoney;
    }

    public String getRoleID() {
        return roleID;
    }

    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getChannelOrderID() {
        return channelOrderID;
    }

    public void setChannelOrderID(String channelOrderID) {
        this.channelOrderID = channelOrderID;
    }

    public Date getBeginCreateTime() {
        return beginCreateTime;
    }

    public void setBeginCreateTime(Date beginCreateTime) {
        this.beginCreateTime = beginCreateTime;
    }

    public Date getEndCreateTime() {
        return endCreateTime;
    }

    public void setEndCreateTime(Date endCreateTime) {
        this.endCreateTime = endCreateTime;
    }
}
