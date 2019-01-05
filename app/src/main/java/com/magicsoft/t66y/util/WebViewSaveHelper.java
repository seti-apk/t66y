package com.magicsoft.t66y.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class WebViewSaveHelper
        implements ActivityCompat.PermissionCompatDelegate {

    private static final int REQUEST_WRITE_EXTERNAL_PERMISSION = 101;

    public interface Delegate {
        void saveWebViewSuccess(String fileName);
        void saveWebViewFailed();
    }

    public static void saveWebViewToLocalImage(final WebView webView, final Activity activity, final Delegate delegate) {
        webView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
//        webView.setDrawingCacheEnabled(true);
//        webView.buildDrawingCache();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap longImage = Bitmap.createBitmap(webView.getMeasuredWidth(),
                        webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(longImage);  // 画布的宽高和 WebView 的网页保持一致
                Paint paint = new Paint();
                canvas.drawBitmap(longImage, 0, webView.getMeasuredHeight(), paint);
                webView.draw(canvas);

                requestWritePermission(longImage, activity, delegate);
            }
        }, 300);
    }

    private static void writeBitmap(Bitmap bitmap, Delegate delegate) {
        String filePath = Environment.getExternalStorageDirectory().getPath()
                + "/com.magicsoft.t66y/";
        try {
            File appDir = new File(filePath);
            if (!appDir.exists() && !appDir.mkdirs()) {
                Log.e("DIR","mkdir failed");
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.close();
            bitmap.recycle();
            delegate.saveWebViewSuccess(file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            delegate.saveWebViewFailed();
        }
    }

    private static void requestWritePermission(Bitmap bitmap, Activity activity, Delegate delegate) {
        Log.e("PMS", "check permission");
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("PMS", "set delegate");
            ActivityCompat.setPermissionCompatDelegate(new WebViewSaveHelper());
            Log.e("PMS", "request permissions");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_PERMISSION);
        } else {
            writeBitmap(bitmap, delegate);
        }
    }

    @Override
    public boolean requestPermissions(@NonNull Activity activity, @NonNull String[] strings, int i) {
        Log.e("PMS", activity.toString());
        return false;
    }

    @Override
    public boolean onActivityResult(@NonNull Activity activity, int i, int i1, @Nullable Intent intent) {
        Log.e("PMS", intent.toString());
        return false;
    }
}
