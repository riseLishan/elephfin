package com.fx.elephfin.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fx.elephfin.R;
import com.fx.elephfin.result.AlipayResult;
import com.fx.elephfin.util.CommonUtil;
import com.fx.elephfin.util.HttpUtil;
import com.fx.elephfin.util.MD5;
import com.fx.elephfin.util.SharedPreferencesUtils;
import com.fx.elephfin.verupdate.AppVersionInfo;
import com.fx.elephfin.verupdate.DownLoadUtils;
import com.fx.elephfin.verupdate.DownloadApk;
import com.fx.elephfin.verupdate.GetPackageVersion;
import com.solidfire.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.List;

public class MainActivity extends Activity {
    private boolean loadStart = true;
    int newVersionCode = 0;
    String newVersionName = "";
    String appDownLoadUrl = "";

    String TAG="【MainActivity】";

    private long exitTime = 0; //保存第一次点击返回键的时间，2秒之内点两次则退出。

    WebView wv;
    ProgressDialog pd;
    LinearLayout startLayout;
    Handler initHandler;
    ProgressBar webProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        startLayout = (LinearLayout) findViewById(R.id.start_layout);
        webProgressBar = (ProgressBar) findViewById(R.id.webview_progressbar);
        WindowManager wm = this.getWindowManager();//获取屏幕的宽高
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        LinearLayout.LayoutParams ParamsLayout = (LinearLayout.LayoutParams) startLayout.getLayoutParams();
        ParamsLayout.width = width;
        ParamsLayout.height = height;
        startLayout.setLayoutParams(ParamsLayout);//将设置好的布局参数应用到控件中
        startLayout.setVisibility(View.VISIBLE);

        wv = (WebView) findViewById(R.id.wv);

        //初始化(针对WebView的一些设置)
        init();
        //打开首页
        loadHomePage();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //判断有没新版本要升级
        upgrageNewVersion();
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    //初始化(针对WebView的一些设置)
    public void init(){
        pd=new ProgressDialog(MainActivity.this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("正在努力为你加载数据......");

        // 显示或隐藏加载进度条
        initHandler=new Handler(){
            public void handleMessage(Message msg)
            {//定义一个Handler，用于处理下载线程与UI间通讯
                if (!Thread.currentThread().isInterrupted())
                {
                    switch (msg.what)
                    {
                        case 0:
                            //显示启动页
                            startLayout.setVisibility(View.VISIBLE);//0，可见 4，不可见 8，不可见，且不占用布局空间
                            pd.show();
                             break;
                        case 1:
                            //
                            pd.hide();
                            startLayout.setVisibility(View.GONE);
                            injectionToWeb();
                            loadStart = false;
                            //告诉WEB UID
                            break;
                    }
                }
                super.handleMessage(msg);
            }
        };

        // WebView
        wv=(WebView)findViewById(R.id.wv);

        //让WebView支持JavaScript(可执行WEB页上的js方法)
        WebSettings webSetting = wv.getSettings();
        webSetting.setJavaScriptEnabled(true);
        //添加接口让JS调用（现在没用上）
//        wv.addJavascriptInterface(new PayJavaScriptInterface(), "Pay");

        //设置为标准字体
        webSetting.setTextSize(WebSettings.TextSize.NORMAL);

        wv.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                //点击联系客服图标，则打开QQ聊天窗口
                //CommonUtil.debugMsg("shouldOverrideUrlLoading","url= "+url);
                Log.e("--main--","url"+url);
                if	(url.indexOf("message") != -1 && url.indexOf("uin=")!=-1  ) //联系客服
                {
                    //重写点击动作,用webview载入
                    String s = url.substring(url.indexOf("uin="),url.length());
                    String qqUrl = "";
                    if (s.indexOf("&") !=-1)
                        qqUrl = s.substring(0, s.indexOf("&")+1);
                    else
                        qqUrl = s;
                    qqUrl = "mqqwpa://im/chat?chat_type=wpa&"+qqUrl;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(qqUrl))); //打开QQ页面
                    Log.e("--main--","qqurl???????????"+qqUrl);
                    return true;
                } else if	(url.indexOf("recharge.html") != -1)//充值
                {
                    Intent intent=new Intent(MainActivity.this, AlipayActivity.class);
                    startActivityForResult(intent,1);
                    return true;
                } else {
                    return false;
                }
            }

            //保存Cookie给httpCliet使用
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //CommonUtil.debugMsg(TAG,"onPageFinished() url is:"+url);
                /* 将cookie保存起来,下次使用httpCliet时保持同步*/
                String c = CookieManager.getInstance().getCookie(url);
                CommonUtil.SHARE_COOKIES = c;
                Log.e("--main--","cmain"+c);
                SharedPreferencesUtils.setParam(MainActivity.this, "String", c);
                //CommonUtil.debugMsg(TAG,"webview.onPageFinished->SHARE_COOKIES:"+c);
                CookieSyncManager.getInstance().sync(); //同步Cookie
            }

        });

        wv.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view,int progress){//载入进度改变而触发
                if (progress == 100) {
//                    pd.hide();
                    initHandler.sendEmptyMessage(1);//如果全部载入,隐藏进度对话框
                }
                else{
                    pd.show();
                }

                super.onProgressChanged(view, progress);
            }



            //支持js 的Alert()，调试时会用上
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }
        });

    }

    //新版的Android的SDK要求在创建WebView所在的线程中操作它，在其它线程中操作它都会报错误
    public void loadurl(final WebView view,final String url) {
        new Thread() {
            public void run() {
                // 更新主线程ui
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initHandler.sendEmptyMessage(0);
                        view.loadUrl(url);//载入网页
                    }

                });

            }

        }.start();
    }


    //打开首页
    private void loadHomePage(){
        loadurl(wv, CommonUtil.BASE_WEB_URL + "user/index.html");
    }

    /********************************************************************************************
     * 以下是完成支付后的处理
     * ******************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//requestCode 对应startActivityForResult(的第二个参数)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            int payType = data.getExtras().getInt("payType");

                if (payType == 1) { //支付宝
                    loadurl(wv, CommonUtil.BASE_WEB_URL + "user/userCenter.html?");
                }else if (payType == 3) { //银联
                    loadurl(wv, CommonUtil.BASE_WEB_URL + "user/userCenter.html?");
                }

        }
    }


    /********************************************************************************************
     * 自动更新升级
     * ******************************************************************************************/
    //取新版本号
    private void upgrageNewVersion() {
        //1.注册下载广播接收器
        DownloadApk.registerBroadcast(this);
        //2.删除已存在的Apk
        DownloadApk.removeFile(this);
        final String urlStr = "userApi/getNewAndroidVersion/token?";
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpResponse response = HttpUtil.send(HttpUtil.METHOD_GET, CommonUtil.BASE_WEB_URL + urlStr, null, true);
                    String res = EntityUtils.toString(response.getEntity());
                    JSONObject obj = JSON.parseObject(res);

                    try{
                        newVersionCode = Integer.valueOf(CommonUtil.getValueFromJson(obj,"version"));
                        Log.e("--main--","newVersionCode"+newVersionCode);
                    }catch(Exception e){
                        e.printStackTrace();
                        newVersionCode = 0;
                        return;
                    }
                    appDownLoadUrl = CommonUtil.getValueFromJson(obj,"url");
                    AppVersionInfo newVersionInfo = new AppVersionInfo();
                    newVersionInfo.setVersionCode(newVersionCode);
                    newVersionInfo.setApkDownloadUrl(appDownLoadUrl);

                    Message msg= Message.obtain();
                    msg.obj=newVersionInfo;
                    upgrageHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //版本升级处理
    protected android.os.Handler upgrageHandler = new android.os.Handler(){

        public void handleMessage(Message msg) {

            AppVersionInfo newVerInfo = (AppVersionInfo) msg.obj;
            //取得本地APP的版本号
            int thisVercode = GetPackageVersion.getVersionCode(MainActivity.this);
            //System.out.println("【MY】: thisVercode=" + thisVercode + "  newVer=" + newVerInfo.getVersionCode());
            Log.e("--main--","newVerInfo"+newVerInfo.getVersionCode());
            Log.e("--main--","thisVercode"+thisVercode);
            if (newVerInfo.getVersionCode() > thisVercode){
                //打开确认对话框，点“是”跑upgrage()，点“否”则跑unUpgrage()
                System.out.println("【开始提问是否要升级】");
                CommonUtil.confirm(MainActivity.this,"金服小象升级","亲，发现有新的版本，要更新么？",upgrage(newVerInfo.getApkDownloadUrl()),unUpgrage());
                Log.e("--main--","url....."+newVerInfo.getApkDownloadUrl());
            }
        }

    };

    //更新升级
    public Runnable upgrage(final String url){
        return new Runnable() {
            public void run() {
                //启动升级程序
                //如果手机已经启动下载程序，执行downloadApk。否则跳转到设置界面
                if (DownLoadUtils.getInstance(getApplicationContext()).canDownload()) {
                    DownloadApk.downloadApk(MainActivity.this, url, "下载", "金服小象");
                    Toast.makeText(MainActivity.this,"应用正在后台下载哟！请等待下载完成",Toast.LENGTH_LONG).show();
//                    DownloadApk.downloadApk(getApplicationContext(), "http://www.huiqu.co/public/download/apk/huiqu.apk", "Hobbees更新", "Hobbees");
                } else {
                    DownLoadUtils.getInstance(getApplicationContext()).skipToDownloadManager();
                }
            }
        };
    }

    //不更新
    public Runnable unUpgrage(){
        return new Runnable() {
            public void run() {
                //doNothing();跳过升级程
                Toast.makeText(MainActivity.this,"更新版本才可使用哟！",Toast.LENGTH_LONG).show();
            }
        };
    }

    //取UID(macd 地址再MD5加密)
    private String getUID(){
        try {
            WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String uid = wm.getConnectionInfo().getMacAddress();
            uid = MD5.getMD5Str(uid);
            return uid;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    /********************************************************************************************
     * 以上是自动更新升级
     * ******************************************************************************************/

    //给WEB页发送android UID
    private void injectionToWeb(){
        if (loadStart==true){
            String paramStr = "javascript:appsend('" + getUID() + "','android')";
            //此版本暂不实现这个功能
            //loadurl(wv, paramStr);
            loadStart=false; //第一次打开主页才发送
        }
    }

    @Override
    protected void onDestroy() {
       //4.反注册广播接收器
        DownloadApk.unregisterBroadcast(this);
        super.onDestroy();
    }


}


