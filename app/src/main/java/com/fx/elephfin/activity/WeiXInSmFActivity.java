package com.fx.elephfin.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fx.elephfin.R;
import com.fx.elephfin.util.CommonUtil;
import com.fx.elephfin.util.HttpUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeiXInSmFActivity extends BasePermissionActivity implements View.OnClickListener{
    private ImageView SmImg,imgBack;
    private String smStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wei_xin_sm_f);
        SmImg= (ImageView) findViewById(R.id.iv);
        imgBack= (ImageView) findViewById(R.id.iv_back);
        imgBack.setOnClickListener(this);
        Intent intent=getIntent();
        smStr=intent.getStringExtra("sm");
        generate();
        perMission();
        //长按，通过zxing读取图片，判断是否有二维码
        SmImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap obmp = ((BitmapDrawable) (SmImg).getDrawable()).getBitmap();
                int width = obmp.getWidth();
                int height = obmp.getHeight();
                int[] data = new int[width * height];
                obmp.getPixels(data, 0, width, 0, 0, width, height);
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
                BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
                QRCodeReader reader = new QRCodeReader();
                Result re = null;
                try {
                    re = reader.decode(bitmap1);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (ChecksumException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                if (re == null) {
                    showSelectAlert(obmp);
                } else {
                    showSelectAlert(obmp);
                }
            }
        });}

    /**
     * 6.0以上动态获取权限
     */
    public void perMission(){
        BasePermissionActivity.requestRunTimePermission(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION
                        ,Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_PHONE_STATE,Manifest.permission.BODY_SENSORS,Manifest.permission.SEND_SMS}
                , new PermissionListener() {
                    @Override
                    public void onGranted() {//所有权限授权成功

                    }

                    @Override
                    public void onGranted(List<String> grantedPermission) {//授权成功权限集合

                    }

                    @Override
                    public void onDenied(List<String> deniedPermission) {//授权失败权限集合

                    }
                });

    }


    /**
     * 根据字符串生成二维码
     * @param content 生成二维码的字符串
     * @param width 二维码的宽
     * @param height 二维码的高
     * @return
     */
    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二维码设置到ImageView中
     */
    public void generate() {
        Bitmap qrBitmap = generateBitmap(smStr,430, 430);
        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Bitmap bitmap = addLogo(qrBitmap, logoBitmap);
        SmImg.setImageBitmap(bitmap);
    }

    /**
     * 二维码中间加图标
     * @param qrBitmap 二维码
     * @param logoBitmap 中间显示的logo
     * @return
     */
    private Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) {
        int qrBitmapWidth = qrBitmap.getWidth();
        int qrBitmapHeight = qrBitmap.getHeight();
        int logoBitmapWidth = logoBitmap.getWidth();
        int logoBitmapHeight = logoBitmap.getHeight();
        Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blankBitmap);
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        float scaleSize = 1.0f;
        while ((logoBitmapWidth / scaleSize) > (qrBitmapWidth / 5) || (logoBitmapHeight / scaleSize) > (qrBitmapHeight / 5)) {
            scaleSize *= 2;
        }
        float sx = 1.0f / scaleSize;
        canvas.scale(sx, sx, qrBitmapWidth / 2, qrBitmapHeight / 2);
        canvas.drawBitmap(logoBitmap, (qrBitmapWidth - logoBitmapWidth) / 2, (qrBitmapHeight - logoBitmapHeight) / 2, null);
        canvas.restore();
        return blankBitmap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;

        }
    }


    /**
     * 保存图片的dialog
     * @param bitmap
     */
    private void showSelectAlert(final Bitmap bitmap) {
        final AlertDialog myDialog = new AlertDialog.Builder(WeiXInSmFActivity.this).create();
        myDialog.show();
        myDialog.getWindow().setContentView(R.layout.alertdialogdsm);
        myDialog.getWindow().findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentImage();
                myDialog.dismiss();
            }
        });
        myDialog.getWindow().findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
    }


    //这种方法状态栏是空白，显示不了状态栏的信息
    private void saveCurrentImage()
    {
        //获取当前屏幕的大小
        int width = getWindow().getDecorView().getRootView().getWidth();
        int height = getWindow().getDecorView().getRootView().getHeight();
        //生成相同大小的图片
        Bitmap temBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888 );
        //找到当前页面的根布局
        View view =  getWindow().getDecorView().getRootView();
        //设置缓存
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        //从缓存中获取当前屏幕的图片
        temBitmap = view.getDrawingCache();
        SimpleDateFormat df = new SimpleDateFormat("yyyymmddhhmmss");
        String time = df.format(new Date());
        String path="";
        String fileName="";
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
         File   dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera");
            // 判断文件夹是否存在，不存在则创建
            if(!dir.exists()){
                dir.mkdir();
            }
            fileName=time + ".jpg";
            path=Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/" + fileName;
            File file = new File(path);
            // 判断文件是否存在，不存在则创建
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Toast.makeText(WeiXInSmFActivity.this,"截屏失败，请手动截屏！",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                temBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(WeiXInSmFActivity.this,"截屏失败，请手动截屏！",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Toast.makeText(WeiXInSmFActivity.this,"截屏失败，请手动截屏！",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
//            // 其次把文件插入到系统图库
//            try {
//                MediaStore.Images.Media.insertImage(getContentResolver(),
//                        file.getAbsolutePath(), fileName, null);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
            // 最后通知图库更新
         sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
            Toast.makeText(WeiXInSmFActivity.this,"截屏成功，请到相册里查看！",Toast.LENGTH_LONG).show();
        }
    }

}
