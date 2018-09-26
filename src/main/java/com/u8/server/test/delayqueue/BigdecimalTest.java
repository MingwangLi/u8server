package com.u8.server.test.delayqueue;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class BigdecimalTest {

    public static void main(String[] args) {
        Double d1 = 1.0D;
        BigDecimal bigDecimal = new BigDecimal(d1);
        //bigDecimal = bigDecimal.setScale(2,BigDecimal.ROUND_DOWN);  仅仅针对多的位数处理 无法处理不足的位数
        DecimalFormat df = new DecimalFormat("0.00");
        System.out.print(df.format(bigDecimal));
    }
}
