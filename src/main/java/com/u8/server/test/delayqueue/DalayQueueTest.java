package com.u8.server.test.delayqueue;

public class DalayQueueTest {

    public static void main(String[] args) {
        System.out.println("KTV正常营业");
        System.out.println("================================");
        /*final KTV ktv = new KTV();
        Thread sing = new Thread(ktv);
        sing.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ktv.begin("张三", "111", 100);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ktv.begin("李四", "112", 200);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ktv.begin("王五", "113", 300);
            }
        }).start();*/
        //attention:使用延迟队列时 如果执行的任务很长 受到网络波动 耗内存 io资源等情况时 需要使用多线程处理保证能即时在延迟期到来时执行 另外时间使用更为细小的纳秒
        KTV ktv = new KTV();
       ktv.begin("张三", "111", 100);
       ktv.begin("李四", "112", 200);
       ktv.begin("王五", "113", 300);
       new Thread(ktv).start();
       new Thread(ktv).start();
       new Thread(ktv).start();
    }
}
