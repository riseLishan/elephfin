package com.fx.elephfin.verupdate;

/**
 * 接收ndroid.intent.action.DOWNLOAD_COMPLETE广播信息
 * Created by 莫仁周 on 2017/2/22 0022.
 */

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;


public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            System.out.println("【ACTION_DOWNLOAD_COMPLETE】");
            long downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            installApk(context, downloadApkId);
        }
    }

    /**
     * 安装apk
     */
    private void installApk(Context context,long downloadId) {

        long downId = SystemInfo.getInstance().getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
        if(downloadId == downId) {
            DownloadManager downManager= (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = downManager.getUriForDownloadedFile(downloadId);
            SystemInfo.getInstance().setString("downloadApk",downloadUri.getPath());
            Log.e("--main--","downloadApk????????"+downloadUri.getPath());
            if (downloadUri != null) {
                String filePath = downloadUri.getPath();

                File downloadFile = new File(filePath);
                if(null != downloadFile && downloadFile.exists()) {
                    //删除之前先判断用户是否已经安装了，安装了才删除。
                    System.out.println("【文件下载成功】：" + filePath);
                }else
                {
                    System.out.println("【文件下载失败】：文件没下载成功");
                }

                Intent install= new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(downloadUri, "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(install);
            } else {
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
