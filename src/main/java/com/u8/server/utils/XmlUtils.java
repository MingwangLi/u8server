package com.u8.server.utils;

import com.u8.server.sdk.quick.QuickPayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

/**
 * Created by ant on 2015/12/3.
 */
public class XmlUtils {

    private static Logger logger = LoggerFactory.getLogger(XmlUtils.class);

    private static DocumentBuilderFactory dbFactory = null;
    private static DocumentBuilder db = null;
    private static Document document = null;
    //private static List<Book> books = null;
    static{
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            db = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            logger.error("----DocumentBuilderFactory、DocumentBuilder初始化异常:{}",e.getMessage());
        }
    }


    /**
     * 文件解析QucikPayData
     * @param xml
     * @return
     */
    public static QuickPayData readXML(String xml){
        try {
            document = db.parse(new File(xml));
            NodeList message = document.getElementsByTagName("message");
            QuickPayData data = new QuickPayData();
            Element element = (Element)message.item(0);
            String is_test = element.getElementsByTagName("is_test").item(0).getFirstChild().getNodeValue();
            String channel = element.getElementsByTagName("channel").item(0).getFirstChild().getNodeValue();
            String channel_uid = element.getElementsByTagName("channel_uid").item(0).getFirstChild().getNodeValue();
            String game_order = element.getElementsByTagName("game_order").item(0).getFirstChild().getNodeValue();
            String order_no = element.getElementsByTagName("order_no").item(0).getFirstChild().getNodeValue();
            String pay_time = element.getElementsByTagName("pay_time").item(0).getFirstChild().getNodeValue();
            String amount = element.getElementsByTagName("amount").item(0).getFirstChild().getNodeValue();
            String status = element.getElementsByTagName("status").item(0).getFirstChild().getNodeValue();
            String extras_params = element.getElementsByTagName("extras_params").item(0).getFirstChild().getNodeValue();
            data.setIs_test(is_test);
            data.setChannel(channel);
            data.setAmount(amount);
            data.setChannel_uid(channel_uid);
            data.setExtras_params(extras_params);
            data.setGame_order(game_order);
            data.setOrder_no(order_no);
            data.setPay_time(pay_time);
            data.setStatus(status);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("----XmlUtils解析xml异常,异常信息:{}",e.getMessage());
        }
        return null;
    }

    /**
     * String
     * @param args
     * @throws Exception
     */


    public static void  main(String[] args) throws Exception{
        QuickPayData data = readXML("F:\\project\\u8server\\u8server\\src\\main\\java\\com\\u8\\server\\utils\\quick.xml");
        System.out.println(data);
    }

}
