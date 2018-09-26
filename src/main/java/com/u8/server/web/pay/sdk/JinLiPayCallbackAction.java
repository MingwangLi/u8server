package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;

import com.u8.server.log.Log;
import com.u8.server.web.pay.SendAgent;
import org.apache.log4j.Logger;

import com.u8.server.sdk.jinli.jinli.RSASignature;
import com.u8.server.service.UOrderManager;

import org.apache.commons.lang.CharEncoding;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * 金立SDK充值回调接口
 * Created by xiaopao on 2015/12/6.
 */

@Controller
@Namespace("/pay/jinli")
public class JinLiPayCallbackAction extends UActionSupport{
	Logger logger = Logger.getLogger(this.getClass());
	
	//注意：Amigo Play Server只将支付成功的订单进行返回，未成功订单不会有通知；商户服务器需要确保NotifyURL 稳定可靠。
	
    private String api_key;             //必须 	商户APIKey
    private String close_time;          //必须 	支付订单关闭时间
    private String create_time; 	    //必须 	支付订单创建时间
    private String  deal_price; 	        //必须 	商品总金额
    private String out_order_no; 	    //必须 	商户订单号
    private String pay_channel; 	    //必须 	用户支付方式(A币支付：100)
    private String submit_time; 	    //必须 	商户提交订单时间
    private String user_id; 	        //必须 	文档说返回null？？？
    private String sign; 	            //必须 	签名

    @Autowired
    private UOrderManager orderManager;

    @Action("payCallback")
    public void payCallback(){

        try{

			Log.d("api_key:"+api_key);
			Log.d("close_time:"+close_time);
			Log.d("create_time:"+create_time);
			Log.d("deal_price:"+deal_price);
			Log.d("out_order_no:"+out_order_no);
			Log.d("pay_channel:"+pay_channel);
			Log.d("submit_time:"+submit_time);
			Log.d("user_id:"+user_id);
			Log.d("sign:"+sign);

            long orderID = Long.parseLong(out_order_no);

            UOrder order = orderManager.getOrder(orderID);

            if(order == null || order.getChannel() == null){
                logger.debug("The order is null or the channel is null.");
                this.renderState("fail");
                return;
            }

            if(order.getState() > PayState.STATE_PAYING){
                logger.debug("The state of the order is complete. The state is "+order.getState());
                this.renderState("success");
                return;
            }

            if(!order.getChannel().getCpAppKey().equals(this.api_key)){
                logger.debug("The api_key of the order is invalid. The api_key is "+this.api_key+"; the valid appId is "+order.getChannel().getCpAppKey());
                this.renderState("fail");
                return;
            }

            if(isValid(order.getChannel())){
                order.setState(PayState.STATE_SUC);
				order.setRealMoney((int)(Float.valueOf(deal_price) * 100));
				order.setSdkOrderTime(submit_time);
                orderManager.saveOrder(order);
                SendAgent.sendCallbackToServer(this.orderManager, order);
                this.renderState("success");
            }else{
                order.setState(PayState.STATE_FAILED);
                orderManager.saveOrder(order);
                this.renderState("fail");
            }


        }catch (Exception e){
            e.printStackTrace();
            try {
            	this.renderState("fail");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    private boolean isValid(UChannel channel) throws Exception {
        StringBuilder sb = new StringBuilder();
        
        DecimalFormat df = new DecimalFormat(".00");
        String dealPrice = df.format((Float.valueOf(this.deal_price) / 1.00f));

        sb.append("api_key=").append(this.api_key).append("&")
        	.append("close_time=").append(this.close_time).append("&")
	        .append("create_time=").append(this.create_time).append("&")
	        .append("deal_price=").append(dealPrice).append("&")
	        .append("out_order_no=").append(this.out_order_no).append("&")
	        .append("pay_channel=").append(this.pay_channel).append("&")
	        .append("submit_time=").append(this.submit_time).append("&")
	        .append("user_id=").append(this.user_id==null?"null":this.user_id);


        return RSASignature.doCheck(sb.toString(), this.sign, channel.getCpPayKey(), CharEncoding.UTF_8);
    }

    private void renderState(String resultMsg) throws IOException {


		PrintWriter out = this.response.getWriter();
		out.println(resultMsg);
		out.flush();
		//out.close();
		
		return;        

    }

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getApi_key() {
		return api_key;
	}

	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	public String getClose_time() {
		return close_time;
	}

	public void setClose_time(String close_time) {
		this.close_time = close_time;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getDeal_price() {
		return deal_price;
	}

	public void setDeal_price(String deal_price) {
		this.deal_price = deal_price;
	}

	public String getPay_channel() {
		return pay_channel;
	}

	public void setPay_channel(String pay_channel) {
		this.pay_channel = pay_channel;
	}

	public String getOut_order_no() {
		return out_order_no;
	}

	public void setOut_order_no(String out_order_no) {
		this.out_order_no = out_order_no;
	}


	public String getSubmit_time() {
		return submit_time;
	}

	public void setSubmit_time(String submit_time) {
		this.submit_time = submit_time;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public UOrderManager getOrderManager() {
		return orderManager;
	}

	public void setOrderManager(UOrderManager orderManager) {
		this.orderManager = orderManager;
	}


}
