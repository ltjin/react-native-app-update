package com.yuanyinguoji.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

/**
 * Created by ltjin on 16/9/26.
 */
public class AppUpdateModule extends ReactContextBaseJavaModule {

    private Context mContext;
    private String mAPKName;
    private ProgressDialog mProgressBar;

    public AppUpdateModule(ReactApplicationContext context){
        super(context);
        mContext = context;
    }

    @Override
    public String getName() {
        return "AppUpdate";
    }

    @ReactMethod
    public void update(String url) {
        downFile(url);
    }

    private void downFile(final String url) {
        mProgressBar = new ProgressDialog(getCurrentActivity());    //进度条，在下载的时候实时更新进度，提高用户友好度
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressBar.setCancelable(false);
        mProgressBar.setCanceledOnTouchOutside(false);
        mProgressBar.setTitle("正在下载");
        mProgressBar.setMessage("请稍候...");
        mProgressBar.setProgress(0);
        mProgressBar.show();
        new Thread() {
            public void run() {
                try {
                    mAPKName = UUID.randomUUID().toString() + ".apk";
                    URL downloadURL=new URL(url);
                    HttpURLConnection conn=(HttpURLConnection)downloadURL.openConnection();
                    int length = (int) conn.getContentLength();   //获取文件大小
                    mProgressBar.setMax(length);                            //设置进度条的总长度
                    InputStream is = conn.getInputStream();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        File file = new File(
                                Environment.getExternalStorageDirectory(),
                                mAPKName);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buf=new byte[4*1024];   //这个是缓冲区，即一次读取10个比特，我弄的小了点，因为在本地，所以数值太大一 下就下载完了，看不出progressbar的效果。
                        int ch = -1;
                        int process = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                            process += ch;
                            mProgressBar.setProgress(process);       //这里就是关键的实时更新进度了！
                        }

                    }
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    mProgressBar.cancel();
                    installAPK();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    //安装文件，一般固定写法
    private void installAPK() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), mAPKName)),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

}
