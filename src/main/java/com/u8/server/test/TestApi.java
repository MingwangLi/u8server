package com.u8.server.test;

import com.u8.server.cache.CacheManager;
import com.u8.server.cache.UApplicationContext;
import com.u8.server.constants.PayState;
import com.u8.server.data.*;
import com.u8.server.service.*;
import com.u8.server.utils.IDGenerator;
import com.u8.server.utils.TimeUtils;

import java.sql.Time;
import java.util.*;

/**
 * Created by ant on 2016/9/20.
 */
public class TestApi {

    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;

    public static void runTestData(){
        Calendar calendar = Calendar.getInstance();

        /*** 定制每日11:00执行方法 ***/

        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date date=calendar.getTime(); //第一次执行定时任务的时间

        //如果第一次执行定时任务的时间 小于 当前的时间
        //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。循环执行的周期则以当前时间为准
        if (date.before(new Date())) {
            date = TimeUtils.dateAdd(date, 1);
        }

        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                TestApi.generateTestData();
            }
        };
        //安排指定的任务在指定的时间开始进行重复的固定延迟执行。
        timer.schedule(task,date,PERIOD_DAY);
    }

    public static void generateTestData(){

        List<UChannel> channels = CacheManager.getInstance().getChannelList();
        Random r = new Random(System.nanoTime());

        //#2 随机产生100-5000个登录行为
        int max = r.nextInt(5000) + 100;
        UUserManager userManager = (UUserManager)UApplicationContext.getBean("userManager");
        List<UUser> users = userManager.queryLastUsers(max);
        for(int i=0; i<users.size(); i++){
            generateUserLogin(users.get(i));
        }

        //#1 随机产生10-500个新用户
        max = r.nextInt(500)+10;

        int userCount = 10+r.nextInt(max);

        for(int i=0; i<max; i++){
            UChannel c = channels.get(r.nextInt(channels.size()));
            //生成一条设备信息
            UDevice device = generateDevice(c);
            if(i < userCount){
                generateNewUser(c, device);
            }

        }

    }

    /**
     * 模拟产生一个新用户
     */
    public static void generateNewUser(UChannel channel, UDevice device){

        try{


            //#2 生成一条用户信息
            UUser user = generateUser(channel, device.getDeviceID());

            //#3 模拟游戏内行为
            modelNewUserLogin(user, true);

            //#4 模拟付费
            generateOrder(user);

        }catch(Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 模拟一次已有用户登录和充值行为
     * @param user
     */
    public static void generateUserLogin(UUser user){

        try{

            //#3 模拟游戏内行为
            modelNewUserLogin(user, false);

            //#4 模拟付费
            generateOrder(user);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 模拟一次新用户登录
     * @param user
     */
    public static void modelNewUserLogin(UUser user, boolean newUser){

        Date time = new Date();

        if(newUser)
            generateUserLog(user, 1, time);

        generateUserLog(user, 2, TimeUtils.minuteAdd(time, 1));
        generateUserLog(user, 3, TimeUtils.minuteAdd(time, 5));
        generateUserLog(user, 4, TimeUtils.minuteAdd(time, 30));

    }


    /**
     * 生成一条登录日志
     * @param user
     * @param opType
     * @param time
     */
    private static void generateUserLog(UUser user, int opType, Date time){
        Random r = new Random(System.nanoTime());
        UUserLog log = new UUserLog();
        log.setUserID(user.getId());
        log.setAppID(user.getAppID());
        log.setChannelID(user.getChannelID());
        log.setServerID(r.nextInt(100)+"");
        log.setServerName("server-"+log.getServerID());
        log.setRoleID("r-"+System.currentTimeMillis());
        log.setRoleName("test-"+ System.currentTimeMillis());
        log.setRoleLevel(r.nextInt(80)+"");
        log.setIp("192.168.1.1");
        log.setDeviceID(user.getDeviceID());
        log.setOpTime(time);
        log.setRegTime(user.getCreateTime());
        log.setOpType(opType);

        UUserLogManager logManager = (UUserLogManager)UApplicationContext.getBean("userLogManager");
        logManager.saveUserLog(log);
    }

    /**
     * 生成一个设备信息
     * @return
     */
    private static UDevice generateDevice(UChannel channel){
        String deviceID = UUID.randomUUID().toString();
        UDevice device = new UDevice();
        device.setDeviceID(deviceID);
        device.setAppID(channel.getAppID());
        device.setChannelID(channel.getChannelID());
        device.setMac("8c:be:be:fd:07:db");
        device.setDeviceType("MI 3");
        device.setDeviceOS(1);
        device.setDeviceDpi("1080×1920");
        device.setArea("CN");
        device.setIp("192.168.1.1");
        device.setCreateTime(new Date());

        UDeviceManager deviceManager = (UDeviceManager)UApplicationContext.getBean("deviceManager");
        deviceManager.saveDevice(device);

        return device;
    }

    /**
     * 生成一个新用户
     * @param channel
     * @param deviceID
     * @return
     */
    private static UUser generateUser(UChannel channel, String deviceID){
        UUser user = new UUser();
        user.setAppID(channel.getAppID());
        user.setChannelID(channel.getChannelID());
        user.setName(System.currentTimeMillis() + channel.getMaster().getNameSuffix());
        user.setChannelUserID(channel.getMaster().getSdkName()+"-"+System.currentTimeMillis());
        user.setChannelUserName("test-"+System.currentTimeMillis());
        user.setChannelUserNick("");
        Date now = new Date();
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        user.setDeviceID(deviceID);
        user.setFirstCharge(1);
        user.setFirstChargeTime(new Date());

        UUserManager userManager = (UUserManager)UApplicationContext.getBean("userManager");
        userManager.saveUser(user);

        return user;
    }

    private static UOrder generateOrder( UUser user){
        Random r = new Random(System.nanoTime());
        UOrder order = new UOrder();
        order.setOrderID(IDGenerator.getInstance().nextOrderID());
        order.setAppID(user.getAppID());
        order.setChannelID(user.getChannelID());
        order.setMoney(600 + r.nextInt(10000));
        order.setProductID((r.nextInt(100)+1)+"");
        order.setProductName("p-"+order.getProductID());
        order.setProductDesc("");
        order.setCurrency("RMB");
        order.setUserID(user.getId());
        order.setUsername(user.getName());
        order.setExtension("");
        order.setState(PayState.STATE_COMPLETE);
        order.setChannelOrderID("");
        order.setRoleID("r-"+ System.currentTimeMillis());
        order.setRoleName("r-"+System.currentTimeMillis());
        order.setServerID(r.nextInt(100)+"");
        order.setServerName("server-"+order.getServerID());
        order.setCreatedTime(new Date());
        order.setNotifyUrl("localhost:8080/pay/payCallback/");

        UOrderManager orderManager = (UOrderManager)UApplicationContext.getBean("orderManager");
        orderManager.saveOrder(order);

        return order;
    }
}
