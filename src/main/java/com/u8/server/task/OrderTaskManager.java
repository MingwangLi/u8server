package com.u8.server.task;

import com.u8.server.data.UOrder;
import com.u8.server.log.Log;


import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 应用宝SDK相关支付逻辑
 * Created by ant on 2015/10/14.
 */
//@Component("orderTaskManager")
//@Scope("singleton")
public class OrderTaskManager {

    private static final long DELAY_MILLIS = 600000;      //每次延迟执行间隔,ms. 这里是10分钟
    private static final int MAX_RETRY_NUM = 6;         //最多重试6次

    private static volatile OrderTaskManager instance;

    private DelayQueue<OrderTask> tasks;

    private ExecutorService executor;

    //private volatile boolean isRunning = false;

    private OrderTaskManager(){
        this.tasks = new DelayQueue<>();
        executor = Executors.newFixedThreadPool(3);
    }

    public static OrderTaskManager getInstance(){
        if(null == instance){
            synchronized (OrderTaskManager.class){
                if(null == instance) {
                    instance = new OrderTaskManager();
                }
            }
        }
        return instance;
    }


    //添加一个新支付请求到队列中
    public void addOrder(UOrder order){

        OrderTask task = new OrderTask(order, 1000, MAX_RETRY_NUM);
        //this.tasks.add(task);
        this.tasks.offer(task);
        execute();
    }

    public void execute(){
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try{
                        //如果延迟时间没有到 当前线程阻塞 添加和移除元素内部都有加锁机制 是线程安全的
                        OrderTask task = tasks.take();
                        task.run();
                        if(task.getState() == OrderTask.STATE_RETRY){
                            task.setDelay(DELAY_MILLIS * task.getRetryCount());
                            tasks.add(task);
                        }else if(task.getState() == OrderTask.STATE_FAILED){
                            Log.e("the order %s send to game server failed.", task.getOrder().getOrderID());
                        }
                }catch (Exception e){
                    Log.e(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public void destory(){
        if(executor != null){
            executor.shutdownNow();
            executor = null;
        }
    }


}
