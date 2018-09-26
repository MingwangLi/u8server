package com.u8.server.test;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TempTest {
    public static void main(String[] args) {

        Double money = 1.0D;
        //BigDecimal  bigDecimal = new BigDecimal(money);
        //bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
        //System.out.println(bigDecimal.toString());  多位截取 不足位无法补齐


        DecimalFormat df = new DecimalFormat("#.00");
        String format = df.format(money);
        System.out.println(format);


        System.out.println(String.format("%.2f",money));
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        System.out.println(numberFormat.format(money));  //no



    }
}
