package com.u8.server.test.delayqueue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class KTVConsumer implements Delayed {

    private String name;
    //截止时间
    private long endTime;
    //包厢号
    private String boxNum;

    public KTVConsumer(String name,String boxNum,long endTime){
        this.name=name;
        this.boxNum=boxNum;
        this.endTime=endTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(endTime-System.currentTimeMillis(),TimeUnit.MILLISECONDS);
    }



    @Override
    public int compareTo(Delayed delayed) {
        if ((null == delayed) || !(delayed instanceof KTVConsumer)) {
            return 1;
        }
        if (this == delayed) {
            return 0;
        }
        KTVConsumer ktvConsumer = (KTVConsumer) delayed;
        return endTime - ktvConsumer.getEndTime() > 0?1:(endTime - ktvConsumer.getEndTime() == 0?0:-1);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getBoxNum() {
        return boxNum;
    }

    public void setBoxNum(String boxNum) {
        this.boxNum = boxNum;
    }
}
