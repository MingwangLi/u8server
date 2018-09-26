package com.u8.server.test.delayqueue;

import java.util.concurrent.DelayQueue;

public class KTV implements Runnable{

    private DelayQueue<KTVConsumer> delayQueue = new DelayQueue<>();


    public void begin(String name,String boxNum,int money){

        KTVConsumer man = new KTVConsumer(name,boxNum,100*money+System.currentTimeMillis());
        System.out.println(man.getName()+" 等人交了"+money+"元钱，进入"+man.getBoxNum()+"号包厢,开始K歌...");
        this.delayQueue.offer(man);
    }

    public void end(KTVConsumer man){
        System.out.println(man.getName()+" 等人所在的"+man.getBoxNum()+"号包厢,时间到...");
    }


    @Override
    public void run() {
        //while (true){
            try {
                KTVConsumer ktvConsumer = delayQueue.take();
                end(ktvConsumer);
                System.out.println("任务执行时间:"+System.currentTimeMillis());
                //模拟好处理很多业务
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
       //}
    }
}
