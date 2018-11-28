package com.fx.elephfin.util;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fx.elephfin.R;

import java.util.Map;

/**
 * Created by 莫仁周 on 2017/2/16 0016.
 */

public class CommonUtil {

    public static Boolean PUBLISH_VERSION = true; //是否正式版
    public static String BASE_WEB_URL = "";
    public static String SHARE_COOKIES = "";


    public static void ShowMesssage(Context context, String msg){
        Toast toast = Toast.makeText(context,
                msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imageCodeProject = new ImageView(context);
        imageCodeProject.setImageResource(R.mipmap.ic_launcher);
        toastView.addView(imageCodeProject, 0);
        toast.show();
    }


    /**
     * 同步一下cookie
     */
    public static void synCookies(Context context, String url) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除
        cookieManager.setCookie(url, CommonUtil.SHARE_COOKIES);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();
    }

    public static void inits(Boolean publicVerion){
        PUBLISH_VERSION = publicVerion;
        if (publicVerion == false){//测试服务器
            BASE_WEB_URL = "http://120.76.127.109:8080/jffront/jf/";
//            BASE_WEB_URL = "http://192.168.0.97:8080/jf/";
        }else {//生产环境服务器
            BASE_WEB_URL = "http://www.elephfin.com/jf/";
            //BASE_WEB_URL = "http://120.25.69.185/jf/";
        }
    }

    //遍历Map
    public static  String getMapValue(Map<String, String> map,String key) {
        String value="";
        for (String key1 : map.keySet()) {
            value = map.get(key1);
            if (key1.equals(key)){
                break;
            }
        }
        return  value;
    }

    //确认对话框
    public static void confirm(Context context,String title,String showMsg,Runnable aProcedure, Runnable bProcedure){
        DialogHandle appdialog = new DialogHandle();
        boolean dlg = appdialog.Confirm(context, title, showMsg,"否", "是", aProcedure, bProcedure);
    }


    //返回json任意层Key的值
    public static String getValueFromJson(JSONObject jsonObj, String key){
        String resultStr = "";
        //Iterator list = jsonObj.keySet();
        for (String datakey: jsonObj.keySet()){
            if (datakey==null){
                continue;
            }
            String v1 = jsonObj.getString(datakey);
            if (datakey.equals(key)){
                resultStr = v1;
                break;
            }
            if (v1 != null && v1.length() > 0 && !v1.equals("null")){
                if (v1.substring(0,1).equals("{")){
                    JSONObject obj = JSON.parseObject(v1);
                    String v2 = getValueFromJson(obj,key);
                    if (v2 != null && !v2.equals("")){
                        resultStr = v2;
                        break;
                    }
                }
            }
        }
        return  resultStr;
    }

    public static void debugMsg(String Tag,String msg){
        Log.d("【"+Tag+"】", msg);
        //System.out.println("【"+Tag+"】"+msg);
    }
}
