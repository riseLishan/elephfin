package com.fx.elephfin.wxpay;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.fx.elephfin.activity.AlipayActivity;
import com.switfpass.pay.MainApplication;
import com.switfpass.pay.activity.PayPlugin;
import com.switfpass.pay.bean.RequestMsg;
import com.switfpass.pay.utils.MD5;
import com.switfpass.pay.utils.SignUtils;
import com.switfpass.pay.utils.Util;
import com.switfpass.pay.utils.XmlUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by 莫仁周 on 2017/3/4 0004.
 */

public class WftWxPay {

    //public android.os.Handler afterWxpayHandler;


    Activity activity;
    Context context=null;
    public static AlipayActivity at;

    public void setWftWxPay(Activity a,Context c) {
        activity = a;
        context = c;
    }

    ////微信支付完成后接收此通知
    public static android.os.Handler afterWxpayHandler = new android.os.Handler(){
        public void handleMessage(Message msg) {
            int errCode = (Integer) msg.obj;
            at.afterWxpay(errCode);
        }
    };


    /********************************************************************************************
     * 以下内容都不用了，这些处理过程都已在服务器上实现，只是调试时用到而已
     * ******************************************************************************************/

    public void payBegin(){
        new GetPrepayIdTask().execute();
    }




    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>>
    {

        private ProgressDialog dialog;

        private String accessToken;

        public GetPrepayIdTask(String accessToken)
        {
            this.accessToken = accessToken;
        }

        public GetPrepayIdTask()
        {
        }

        @Override
        protected void onPreExecute()
        {
            dialog = ProgressDialog.show(context,"提示","正在获取预支付订单...");
        }

        @Override
        protected void onPostExecute(Map<String, String> result)
        {
            if (dialog != null)
            {
                dialog.dismiss();
            }
            if (result == null)
            {
                Toast.makeText(context, "获取预下单失败，原因%s", Toast.LENGTH_LONG).show();
            }
            else {
                if (result.get("status").equalsIgnoreCase("0")) // 成功
                {
                    System.out.println("【获取预下单成功result】/r/n" + result);
                    Toast.makeText(context, "获取预下单成功", Toast.LENGTH_LONG).show();
                    RequestMsg msg = new RequestMsg();
                    msg.setTokenId(result.get("token_id"));
                    msg.setTradeType(MainApplication.WX_APP_TYPE);
                    //msg.setAppId("wx2a5538052969956e");// wx2a5538052969956e
                    msg.setAppId("wx1e71b8d355c57754");//
                    System.out.println("【msg】/r/n" + msg);
                    PayPlugin.unifiedAppPay(activity, msg);
                } else {
                    Toast.makeText(context, "获取预下单失败，原因%s", Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(Void... params)
        {
            // 统一预下单接口
            //            String url = String.format("https://api.weixin.qq.com/pay/genprepay?access_token=%s", accessToken);
            String url = "https://pay.swiftpass.cn/pay/gateway";
            String entity = getParams();
            byte[] buf = Util.httpPost(url, entity);
            if (buf == null || buf.length == 0)
            {
                return null;
            }
            String content = new String(buf);
            try
            {
                return XmlUtils.parse(content);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 组装参数
     * <此方法是服务端调预下单接口时传入具体参数>
     * @return
     * @see [类、类#方法、类#成员]
     */
    private String getParams()
    {

        Map<String, String> params = new HashMap<String, String>();
        params.put("body", "充值测试"); // 商品名称
        params.put("service", "unified.trade.pay"); // 统一下单接口类型
        params.put("version", "2.0"); // 版本
        //params.put("mch_id", "755437000006"); // 威富通商户号
        params.put("mch_id", "101570015954"); // 威富通商户号

        params.put("notify_url", " "); // 后台通知url，此处是空格，商户方开发时需用自己的通知地址
        params.put("nonce_str", genNonceStr()); // 随机数
        String out_trade_no = genOutTradNo();
        //String out_trade_no =dateFormat.format(new Date()).toString();
        params.put("out_trade_no", out_trade_no); //订单号
        Log.i("hehui", "out_trade_no-->" + out_trade_no);
        params.put("mch_create_ip", "127.0.0.1"); // 机器ip地址
        params.put("total_fee", "1"); // 总金额
        params.put("limit_credit_pay", "0"); // 是否禁用信用卡支付， 0：不禁用（默认），1：禁用
        String sign = createSign("c68a0916d14c812d62d462e8be84742c", params); // 9d101c97133837e13dde2d32a5054abb 威富通密钥

        params.put("sign", sign); // sign签名

        return XmlUtils.toXml(params);
    }

    private String genNonceStr()
    {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private String genOutTradNo()
    {
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMddHHmmssddd");
        char c1=(char)(int)(Math.random()*26+97);//随机一个字母
        char c2=(char)(int)(Math.random()*26+97);
        String paymentID =dateFormat.format(new Date()).toString()+String.valueOf(c1)+String.valueOf(c2);// 订单ID
        //Random random = new Random();
        //return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
        return paymentID;
    }

    public String createSign(String signKey, Map<String, String> params)
    {
        StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
        SignUtils.buildPayParams(buf, params, false);
        buf.append("&key=").append(signKey);
        String preStr = buf.toString();
        String sign = "";
        // 获得签名验证结果
        try
        {
            sign = MD5.md5s(preStr).toUpperCase();
        }
        catch (Exception e)
        {
            sign = MD5.md5s(preStr).toUpperCase();
        }
        return sign;
    }
}
