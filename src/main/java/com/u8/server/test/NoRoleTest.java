package com.u8.server.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @Author: lizhong
 * @Des:
 * @Date: 2018/5/2 18:39
 * @Modified:
 */
public class NoRoleTest {
    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\MingwangLi\\Desktop\\1.txt";
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String readLine = null;
        //StringBuilder sb = new StringBuilder();
        Long total = 0L;
        while((readLine = br.readLine()) != null) {
            total += Long.parseLong(readLine);
        }
        System.out.println(total);
    }
}
