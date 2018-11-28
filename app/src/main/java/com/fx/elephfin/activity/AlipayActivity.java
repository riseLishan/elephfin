package com.fx.elephfin.activity;

/**
 * 支付宝支付
 * Created by 莫仁周 on 2017/2/16 0016.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.fx.elephfin.R;
import com.fx.elephfin.result.PayResult;
import com.fx.elephfin.util.CommonUtil;
import com.fx.elephfin.util.HttpUtil;
import com.fx.elephfin.wxpay.WftWxPay;

import com.switfpass.pay.MainApplication;
import com.switfpass.pay.activity.PayPlugin;
import com.switfpass.pay.bean.RequestMsg;
import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.Map;

public class AlipayActivity extends Activity {
    Button btnReturn;
    Button btnOk;
    EditText edtAmout;
    RelativeLayout btnPayAli; //支付宝
//    RelativeLayout btnPayWx;  //微信
    RelativeLayout btnPayyl;  //银联
    RelativeLayout btnPaysm;  //扫码支付
    TextView tvPaytype; //支付方式

    String payAmout  = "0";//金额
    String orderNo = "";
    String payOrderId = "";

    int payType = 0;//1  支付宝支付，3银联支付

    int resultCode = RESULT_CANCELED;

    private static final int SDK_PAY_FLAG = 1;

    private  String token= "null";
    private  String tn= "null";

    /**
     * 扫描跳转Activity RequestCode
     */
    public static final int REQUEST_CODE = 111;
    private static final String TN_URL_01 = "http://101.231.204.84:8091/sim/getacptn";
    private static final String R_SUCCESS = "success";
    private static final String R_FAIL = "fail";
    private static final String R_CANCEL = "cancel";
    private ProgressDialog pd;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            // “00” – 银联正式环境
            // “01” – 银联测试环境，该环境中不发生真实交易
            String tn = (String) msg.obj;
            if (!TextUtils.isEmpty(tn)) {
                // 测试环境
                pd.hide();
                String serverMode = "00";
                UPPayAssistEx.startPayByJAR(AlipayActivity.this,
                        PayActivity.class, null, null, tn, serverMode);
            }
        }
    };
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alipay);
        btnReturn = (Button) findViewById(R.id.btn_return);
        btnOk = (Button) findViewById(R.id.btn_ok);
        edtAmout = (EditText) findViewById(R.id.edt_amout);
        tvPaytype = (TextView) findViewById(R.id.tv_paytype);
        btnPayAli = (RelativeLayout) findViewById(R.id.rv1);
//        btnPayWx = (RelativeLayout) findViewById(R.id.rv2);
        btnPayyl = (RelativeLayout) findViewById(R.id.rv3);
        btnPaysm = (RelativeLayout) findViewById(R.id.rv4);
        payType = 0; //1支付宝支付 3银联支付 0未选择
        tvPaytype.setText("选择支付方式: ");
        btnReturn.setOnClickListener(listener);
        btnOk.setOnClickListener(listener);
        btnPaysm.setOnClickListener(listener);
//        btnPayWx.setOnClickListener(listener);
        btnPayyl.setOnClickListener(listener);
        btnPayAli.setOnClickListener(listener);
    }


    //监听点击事件
    Button.OnClickListener listener = new Button.OnClickListener() {//创建监听对象
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rv1: //选择阿里支付
                    payType = 1;
//                    btnPayWx.setAlpha(1.0f);
                    btnPayAli.setAlpha(0.7f);
                    btnPayyl.setAlpha(1.0f);
                    btnPaysm.setAlpha(1.0f);
                    tvPaytype.setText("选择支付方式: 支付宝支付");
                    break;

                case R.id.rv3: //选择银联支付
                    payType = 3;
                    btnPayyl.setAlpha(0.7f);
                    btnPayAli.setAlpha(1.0f);
                    btnPaysm.setAlpha(1.0f);
                    tvPaytype.setText("选择支付方式: 银联支付");
                    break;
                case R.id.rv4: //选扫码支付
                    payType = 4;
                    btnPayyl.setAlpha(1.0f);
                    btnPayAli.setAlpha(1.0f);
                    btnPaysm.setAlpha(0.7f);
                    tvPaytype.setText("选择支付方式: 扫码支付");
                    break;

                case R.id.btn_return: //返回
                    finish();
                    break;
                case R.id.btn_ok://支付
                    double dAmout = 0.0f;
                    String sAmout = edtAmout.getText().toString();
                    try{
                        dAmout = Double.valueOf(sAmout);
                    }catch (Exception e){
                        e.printStackTrace();
                        dAmout = 0.00;
                        CommonUtil.ShowMesssage(AlipayActivity.this,"金额错误");
                        break;
                    }

                    if (dAmout==0){
                        CommonUtil.ShowMesssage(AlipayActivity.this,"充值金额必须输入");
                        break;
                    }

                    if (CommonUtil.PUBLISH_VERSION == true){
                        int iAmount = (int)dAmout;
                        if (Double.valueOf((int)dAmout) != dAmout){
                            CommonUtil.ShowMesssage(AlipayActivity.this,"充值金额必须为整数");
                            break;
                        }

                        if (dAmout < 50){
                            CommonUtil.ShowMesssage(AlipayActivity.this,"充值金额必须>=50");
                            break;
                        }
                        if (dAmout > 5000){
                            CommonUtil.ShowMesssage(AlipayActivity.this,"单次充值金额不能大于5000");
                            break;
                        }
                    }

                    if (payType!=1 && payType !=3&& payType !=4){
                        CommonUtil.ShowMesssage(AlipayActivity.this,"请先选择支付方式");
                        break;
                    }
//                    Toast.makeText(AlipayActivity.this,"等待发起支付完成",Toast.LENGTH_LONG).show();
                    if (payType==1){ //支付宝
                        final String urlStr = "userApi/getAlipayParam/token?total_amout=" + String.valueOf(dAmout);
                        //加签(访问服务器)->支付
                        SignFromServer(urlStr);
                        break;
                    }else if (payType==3) {  //银联
                        int Amout=0;
                        Amout = Integer.valueOf(sAmout);
                        final String urlStr = "unionpay/pay.html?feeAmount=" + String.valueOf(Amout);
                        pd=new ProgressDialog(AlipayActivity.this);
                        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pd.setMessage("等待支付完成......");
                        pd.show();
                       getTn(urlStr);
                    }else if(payType==4){//扫码
//                        int Amouts=0;
//                        Amouts = Integer.valueOf(sAmout);
//                        final String urlStr = "wxscan/scanPay?payCount=" + String.valueOf(Amouts);
                        final String urlStr = "wxscan/scanPay?payCount=" + String.valueOf(dAmout);
                        pd=new ProgressDialog(AlipayActivity.this);
                        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pd.setMessage("二维码生成中......");
                        pd.show();
                        SmFromServer(urlStr);
                    }

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase(R_SUCCESS)) {
            Toast.makeText(AlipayActivity.this,"支付成功",Toast.LENGTH_LONG).show();
            Intent in = new Intent();
            in.putExtra( "payType", payType);//1  支付宝支付，2微信支付,3银联
            setResult( RESULT_OK, in );
            finish();
        } else if (str.equalsIgnoreCase(R_FAIL)) {
            Toast.makeText(AlipayActivity.this,"支付失败",Toast.LENGTH_LONG).show();
        } else if (str.equalsIgnoreCase(R_CANCEL)) {
            Toast.makeText(AlipayActivity.this,"支付取消",Toast.LENGTH_LONG).show();
        }


    }

    /**
     * 获取扫码支付的字符串
     * @param urlStr
     */
    private void  SmFromServer(final String urlStr){
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpResponse response = HttpUtil.send(HttpUtil.METHOD_GET, CommonUtil.BASE_WEB_URL + urlStr, null, true);
                    String res = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObj = JSON.parseObject(res);
                    if (jsonObj != null){
                        if ("true".equals(CommonUtil.getValueFromJson(jsonObj,"success"))){
                            pd.dismiss();
                            String S = CommonUtil.getValueFromJson(jsonObj,"qr_code");
                            Intent intent=new Intent(AlipayActivity.this,WeiXInSmFActivity.class);
                            intent.putExtra("sm",S);
                            startActivity(intent);
                        }else{
                            //显示错误信息
                            CommonUtil.ShowMesssage(getApplicationContext(),CommonUtil.getValueFromJson(jsonObj,"message"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /********************************************************************************************
     * 以下是支付宝支付
     * ******************************************************************************************/
    //支付宝支付前加签
    private void SignFromServer(final String urlStr) {
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpResponse response = HttpUtil.send(HttpUtil.METHOD_GET, CommonUtil.BASE_WEB_URL + urlStr, null, true);
                    String res = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObj = JSON.parseObject(res);
                    Log.e("--main--",HttpUtil.METHOD_GET+CommonUtil.BASE_WEB_URL + urlStr);
                    if (jsonObj != null){
                        if ("true".equals(CommonUtil.getValueFromJson(jsonObj,"success"))){
                            String S = CommonUtil.getValueFromJson(jsonObj,"orderInfo");
                            token = CommonUtil.getValueFromJson(jsonObj,"token");
                            Message msg= Message.obtain();
                            msg.obj=S;
                            signHandler.sendMessage(msg);
                        }else{
                            //显示错误信息
                            CommonUtil.ShowMesssage(getApplicationContext(),CommonUtil.getValueFromJson(jsonObj,"message"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    /********************************************************************************************
     * 以下是银联支付，获取tn
     * ******************************************************************************************/
    //支付宝支付前加签
    private void getTn(final String urlStr) {
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpResponse response = HttpUtil.send(HttpUtil.METHOD_GET, CommonUtil.BASE_WEB_URL + urlStr, null, true);
                    Log.e("--main--",HttpUtil.METHOD_GET+CommonUtil.BASE_WEB_URL + urlStr);
                    String res = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObj = JSON.parseObject(res);
                    if (jsonObj != null){
                        if ("true".equals(CommonUtil.getValueFromJson(jsonObj,"success"))){
                            tn = CommonUtil.getValueFromJson(jsonObj,"data");
                            Message msg = mHandler.obtainMessage();
                            msg.obj = tn;
                            mHandler.sendMessage(msg);
                        }else{
                            //显示错误信息
                            CommonUtil.ShowMesssage(getApplicationContext(),CommonUtil.getValueFromJson(jsonObj,"message"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    //支付在后台加签返回
    private android.os.Handler signHandler = new android.os.Handler(){
        public void handleMessage(Message msg) {
            String orderInfo = (String) msg.obj;
            if (orderInfo != null){
               //CommonUtil.debugMsg("加签返回orderInfo=",orderInfo.toString());
               //开始支付
               BeginPay(orderInfo);
            }
        }
    };

    //开始支付;
    private void BeginPay(final String orderInfo) {
        new Thread() {
            @Override
            public void run(){
                try {
                    PayTask alipay = new PayTask(AlipayActivity.this);
                    Map<String, String> result = alipay.payV2(orderInfo, true);
                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = result;
                    payHandler.sendMessage(msg);

                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        }.start();
    }

    //支付完成
    private android.os.Handler payHandler = new android.os.Handler(){
        public void handleMessage(Message msg) {
            Map<String, String> result = (Map<String, String>)msg.obj;

                PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                if (payResult == null || payResult.toString().equals("")){
                    Toast.makeText(AlipayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                String resultStr = payResult.getResult();
                Log.e("--main--","resultStr"+resultStr);
                if (resultStr == null || resultStr.toString().equals("")){
                    Toast.makeText(AlipayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                 同步不需要验证
                 */
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为9000则代表支付成功
                if (TextUtils.equals(resultStatus, "9000")) {
                    Toast.makeText(AlipayActivity.this, "支付成功", Toast.LENGTH_SHORT).show();

                    JSONObject jsonObj = JSON.parseObject(resultStr);
                    orderNo = CommonUtil.getValueFromJson(jsonObj,"trade_no");
                    payOrderId = CommonUtil.getValueFromJson(jsonObj,"out_trade_no");
                    payAmout = CommonUtil.getValueFromJson(jsonObj,"total_amount");

                    resultCode = RESULT_OK;
                    Intent in = new Intent();
                    in.putExtra( "token", token);
                    in.putExtra( "orderNo", orderNo);
                    in.putExtra( "payAmout", payAmout);
                    in.putExtra( "payOrderId", payOrderId);
                    in.putExtra( "payType", payType);//1  支付宝支付，2微信支付
                    setResult( resultCode, in );
                    Log.e("--main--","3???????true");
                    finish();

                } else {
                    // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                    Toast.makeText(AlipayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                    resultCode = RESULT_CANCELED;
                    payAmout  = "0.00";//金额
                    orderNo = "";
                    payOrderId = "";
                }
        }
    };
    /********************************************************************************************
     * 以上是支付宝支付的处理
     * ******************************************************************************************/

    /********************************************************************************************
     * 以上是微信支付的处理
     * ******************************************************************************************/
    //统一预下单
    private void wxPayStep1(final String urlStr) {
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpResponse response = HttpUtil.send(HttpUtil.METHOD_GET, CommonUtil.BASE_WEB_URL + urlStr, null, true);
                    String res = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObj = JSON.parseObject(res);
                    String success = CommonUtil.getValueFromJson(jsonObj,"success");
                    if (success.equals("false")){
                        String message = CommonUtil.getValueFromJson(jsonObj,"message");

                        CommonUtil.ShowMesssage(AlipayActivity.this,message) ;
                    }else {
                        Log.e("--main--","微信1true????????");
                        Message msg = Message.obtain();
                        msg.obj = jsonObj;
                        wxpayHandler.sendMessage(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //统一预下单成功后
    private android.os.Handler wxpayHandler = new android.os.Handler(){
        public void handleMessage(Message msg) {
            JSONObject jsonObj = (JSONObject) msg.obj;
            wxPayStep2(jsonObj);
        }
    };
    //支付
    private void wxPayStep2(JSONObject jsonObj) {

        if ("true".equals(CommonUtil.getValueFromJson(jsonObj,"success"))) // 成功
        {
            Toast.makeText(AlipayActivity.this, "获取预下单成功", Toast.LENGTH_LONG).show();
            RequestMsg msg = new RequestMsg();
            msg.setTokenId(CommonUtil.getValueFromJson(jsonObj,"token_id"));
            msg.setTradeType(MainApplication.WX_APP_TYPE);
            msg.setAppId("wx1e71b8d355c57754");//
            WftWxPay.at = this;
            PayPlugin.unifiedAppPay(this, msg);
        } else {
            Toast.makeText(AlipayActivity.this, "获取预下单失败，原因%s", Toast.LENGTH_LONG).show();
        }
    }

    //支付完成后
    public void afterWxpay(int rstCode) {
        CommonUtil.ShowMesssage(AlipayActivity.this,"微信支付完成");
        if (rstCode == 0){
            Intent in = new Intent();
            in.putExtra("payType", payType);//1  支付宝支付，2微信支付
            setResult(RESULT_OK, in );
            finish();
        }
    }

    /********************************************************************************************
     * 以上是微信支付
     * ******************************************************************************************/
}
