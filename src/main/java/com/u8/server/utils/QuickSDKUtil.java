package com.u8.server.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuickSDKUtil {

    private final static Pattern pattern = Pattern.compile("\\d+");

    private final static String charset="utf-8";

    public static String encode(String src,String key) {
        try {
            byte[] data = src.getBytes(charset);
            byte[] keys = key.getBytes();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                int n = (0xff & data[i]) + (0xff & keys[i % keys.length]);
                sb.append("@" + n);
            }
            return sb.toString();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return src;
    }

    public static String decode(String src,String key) {
        if(src == null || src.length() == 0){
            return src;
        }
        Matcher m = pattern.matcher(src);
        List<Integer> list = new ArrayList<Integer>();
        while (m.find()) {
            try {
                String group = m.group();
                list.add(Integer.valueOf(group));
            } catch (Exception e) {
                e.printStackTrace();
                return src;
            }
        }

        if (list.size() > 0) {
            try {
                byte[] data = new byte[list.size()];
                byte[] keys = key.getBytes();

                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (list.get(i) - (0xff & keys[i % keys.length]));
                }
                return new String(data, charset);
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            return src;
        } else {
            return src;
        }
    }


    public static void main(String[] args) {
        //String nt_data = decode("@116@118@172@160@157@88@168@152@164@170@160@160@163@116@84@99@97@96@90@84@153@158@147@166@156@156@167@157@109@85@134@141@126@100@108@85@81@171@166@148@160@155@152@157@164@165@151@111@85@158@167@86@115@110@108@168@173@156@156@161@163@151@156@152@165@156@167@166@146@159@151@113@110@164@156@164@168@152@153@151@113@108@161@167@147@164@149@170@172@113@106@114@95@156@164@152@172@156@167@167@111@116@149@155@147@165@165@150@161@117@98@110@98@147@160@149@162@158@149@163@118@111@156@158@145@161@159@158@164@150@162@148@158@157@112@111@97@154@159@146@163@165@151@158@146@158@153@161@153@110@108@154@160@148@167@164@149@159@144@174@161@155@114@108@101@156@106@105@106@155@156@150@101@156@105@150@99@99@107@150@153@104@149@107@106@107@107@152@102@152@99@154@154@157@150@115@144@96@91@111@97@154@159@146@163@165@151@158@146@165@161@152@114@108@147@159@153@161@167@155@156@146@160@171@156@156@166@113@109@103@149@155@147@165@165@150@161@150@161@164@151@149@170@114@112@151@145@164@157@146@168@168@148@152@163@119@105@109@100@106@101@112@102@103@99@112@105@103@101@108@102@105@99@103@112@112@99@151@145@164@157@146@168@168@148@152@163@119@116@166@166@151@150@170@145@161@161@117@103@97@101@105@98@99@107@96@113@100@107@97@100@108@112@100@112@111@100@101@103@113@104@108@103@102@109@103@161@165@150@156@169@144@163@166@112@110@163@145@177@147@168@153@157@156@118@101@105@103@104@96@97@114@101@103@107@83@98@108@108@104@106@113@104@104@113@102@162@147@172@143@172@157@161@149@110@115@153@160@168@171@158@167@111@106@102@103@100@111@96@153@159@162@167@165@171@111@113@170@166@147@167@165@171@114@100@108@95@170@172@148@173@171@163@113@109@158@176@171@166@148@164@151@162@148@164@152@164@164@115@115@97@151@171@164@170@149@167@143@160@152@170@148@166@169@110@111@96@166@157@170@167@148@152@157@112@111@97@168@172@154@152@162@165@150@158@143@165@153@167@163@145@158@157@113","87431823277157223084400783960319");
        //System.out.println(nt_data);
        String md5sign = decode("@104@156@106@107@148@107@105@101@101@110@153@100@108@112@147@100@100@149@104@109@103@103@145@110@113@107@107@110@96@105@98@157","87431823277157223084400783960319");
        System.out.println(md5sign);
    }
}
