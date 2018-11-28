package com.fx.elephfin.util;

/**
 * Created by 莫仁周 on 2017/2/22 0022.
 */
import android.app.Application;
import com.fx.elephfin.verupdate.SystemInfo;

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        SystemInfo.init(this);
        CommonUtil.inits(true); //false 为测试版本，true 为正式版本
    }
}
