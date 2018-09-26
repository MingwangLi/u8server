package com.u8.server.web.pay.sdk;

import com.u8.server.common.UActionSupport;
import com.u8.server.constants.PayState;
import com.u8.server.data.UOrder;
import com.u8.server.log.Log;
import com.u8.server.sdk.UHttpAgent;
import com.u8.server.sdk.UHttpFutureCallback;
import com.u8.server.service.UOrderManager;
import com.u8.server.utils.Base64;
import com.u8.server.web.pay.SendAgent;
import net.sf.json.JSONObject;
import org.apache.http.entity.StringEntity;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by xzy on 15/12/21.
 */
@Controller
@Namespace("/pay/apple")
public class AppstoreIAPValidate extends UActionSupport {

    private String orderID;
    private String transactionIdentifier;
    private String productId;
    private String transactionReceipt;

    final String urlSandbox = "https://sandbox.itunes.apple.com/verifyReceipt";
    final String urlProduct = "https://buy.itunes.apple.com/verifyReceipt";

    private UOrder order;

    @Autowired
    private UOrderManager orderManager;

    @Action("validate")
    public void validate() {
        try {
            JSONObject params = new JSONObject();

            order = orderManager.getOrder(Long.parseLong(orderID));

            if (transactionReceipt.startsWith("{"))
            {
                params.put("receipt-data", Base64.encode(transactionReceipt, "UTF-8"));
            }
            else
            {
                params.put("receipt-data", transactionReceipt);
            }

            Log.d("apple iap validate " + transactionReceipt);

            StringEntity entity = new StringEntity(params.toString(), "UTF-8");
            entity.setContentType("application/json");

            final StringEntity httpParams = entity;

            // TODO: 保存receipt记录

            //首先尝试生产环境请求验证
            //如果返回21007状态，转到sandbox环境验证
            UHttpAgent.getInstance().post(urlProduct, null, httpParams, new UHttpFutureCallback() {
                @Override
                public void completed(String content) {
                    Log.d("apple iap validate suc:" + content);
                    JSONObject json = JSONObject.fromObject(content);

                    if (json.getInt("status") == 21007)
                    {
                        UHttpAgent.getInstance().post(urlSandbox, null, httpParams, new UHttpFutureCallback() {
                            @Override
                            public void completed(String content) {

                                Log.d("apple iap validate suc:" + content);
                                JSONObject json = JSONObject.fromObject(content);

                                if (json.getInt("status") == 0)
                                {
                                    //沙盒环境验证成功
                                    OnValidatedSuccess();
                                }
                                else
                                {
                                    OnValidateFail();
                                }
                            }

                            @Override
                            public void failed(String err) {
                                Log.d("apple iap validate error: " + err);
                                //TODO: 更新receipt记录状态
                            }
                        });
                    }
                    else if (json.getInt("status") == 0)
                    {
                        //验证成功
                        OnValidatedSuccess();
                    }
                    else
                    {
                        OnValidateFail();
                    }
                }

                @Override
                public void failed(String err) {
                    Log.d("apple iap validate error: " + err);
                    //TODO: 更新receipt记录状态
                }
            });

            this.renderState(true, "Success");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try {
                this.renderState(false, "未知错误");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void OnValidatedSuccess()
    {
        //TODO: 更新receipt记录状态

        if(order == null || order.getChannel() == null){
            Log.d("The order is null or the channel is null.");
            return;
        }

        if(order.getState() > PayState.STATE_PAYING){
            Log.d("The state of the order is not paying. The state is "+order.getState());
            return;
        }

        if (order.getExtension() == productId)
        {
            order.setCompleteTime(new Date());
            order.setState(PayState.STATE_SUC);
            orderManager.saveOrder(order);
            SendAgent.sendCallbackToServer(orderManager, order);
        }
    }

    private void OnValidateFail()
    {
        //TODO: 更新receipt记录状态
        if(order == null || order.getChannel() == null){
            Log.d("The order is null or the channel is null.");
            return;
        }

        if(order.getState() > PayState.STATE_PAYING){
            Log.d("The state of the order is complete. The state is " + order.getState());
            return;
        }

        if (order.getExtension() == productId)
        {
            order.setCompleteTime(new Date());
            order.setState(PayState.STATE_FAILED);
            orderManager.saveOrder(order);
        }
    }

    private void renderState(boolean suc, String msg) throws IOException {

        PrintWriter out = this.response.getWriter();

        if(suc){
            out.write("SUCCESS");
        }else{
            out.write("FAILURE");
        }
        out.flush();
    }

    public void setTransactionReceipt(String transactionReceipt)
    {
        this.transactionReceipt = transactionReceipt;
    }

    public String getTransactionReceipt()
    {
        return this.transactionReceipt;
    }

    public void setTransactionIdentifier(String transactionIdentifier)
    {
        this.transactionIdentifier = transactionIdentifier;
    }

    public String getTransactionIdentifier()
    {
        return this.transactionIdentifier;
    }

    public void setOrderID(String orderID)
    {
        this.orderID = orderID;
    }

    public String getOrderID()
    {
        return this.orderID;
    }

    public void setProductId(String productId)
    {
        this.productId = productId;
    }

    public String getProductId()
    {
        return this.productId;
    }
}
