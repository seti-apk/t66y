package com.magicsoft.t66y;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.magicsoft.t66y.page.CLPage;
import com.magicsoft.t66y.util.WebViewSaveHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class CLWebViewActivity extends AppCompatActivity
        implements WebViewSaveHelper.Delegate {

    private static final String TAG = "CLWebViewActivity";

    private CLWebViewActivity ui = this;
    boolean isTemplateReady = false;
    private WebView webView;
    public CLPage page;
    boolean isFinished = false;
    boolean isWaiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clweb_view);

        webView = findViewById(R.id.webView);
        initWebView(webView);
//*
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);
//*/
        Intent intent = getIntent();
        final String url = intent.getStringExtra("url");
        Log.w("url", url);
        loadPage(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case R.id.save:
                WebViewSaveHelper.saveWebViewToLocalImage(webView, this, this);
                break;
        }
        return true;
    }

    public synchronized void setTemplateReady(boolean templateReady) {
        isTemplateReady = templateReady;
    }

    void loadPage(String url) {
        setTemplateReady(false);
        try {
            page = CLPage.pageFromUrl(url, this);
            webView.loadUrl(page.template());
        } catch (CLPage.PageClassNotExistsException e) {
            e.printStackTrace();
            finish();
        }
    }

    long pageTime = 0;

    public void pageLoaded(final JSONObject json) {
        Log.d(TAG, "pageLoaded: time spend=" + (System.currentTimeMillis() - pageTime));
//        if (!isTemplateReady) {
//            Log.d(TAG, "pageLoaded: wait");
//                isWaiting = true;
//                webView.wait();
//                isWaiting = false;
//            Log.d(TAG, "pageLoaded: notified, continue");
//        }
        try {
            String title = json.getString("title");
            title = title.replace("&nbsp;", " ");
            ui.setTitle(title);
            webView.loadUrl("javascript:setJson("
                    + json.toString()
                    + ")");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView(WebView webView) {
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        ws.setBlockNetworkImage(false);
        webView.setWebViewClient(webViewClient);

        webView.addJavascriptInterface(new JSInterface(), "jsi");
    }

    @Override
    public void saveWebViewSuccess(String fileName) {
        Toast.makeText(this, "Saved!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void saveWebViewFailed() {
        Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
    }

    public class JSInterface {

        @JavascriptInterface
        public void showImage(String src) {
            Intent intent = new Intent(ui, ImageViewActivity.class);
            intent.putExtra("src", src);
            ui.startActivity(intent);
        }
    }

    final WebViewClient webViewClient = new WebViewClient() {
        long start = 0;
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            start = System.currentTimeMillis();
            super.onPageStarted(view, url, favicon);
            ui.setTitle("Loading...");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished: time spend=" + (System.currentTimeMillis() - start));
            super.onPageFinished(view, url);
            pageTime = System.currentTimeMillis();
            page.loadPage(handler);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // 接受网站证书
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("URL", url);

            try {
                CLPage page = CLPage.pageFromUrl(url, null);
                if (page.same(ui.page)) {
                    Log.e("PAGE", "equals");
                    loadPage(url);
                    return true;
                }
            } catch (CLPage.PageClassNotExistsException e) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(url);
                intent.setData(content_url);
                startActivity(intent);
                return true;
            }

            Intent intent = new Intent(ui, CLWebViewActivity.class);
            intent.putExtra("url", url);
            ui.startActivity(intent);
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        isFinished = true;
    }

//    static class MyHandler extends Handler {
//        WeakReference<CLWebViewActivity> ref;
//
//        MyHandler(CLWebViewActivity activity) {
//            ref = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            CLWebViewActivity activity = ref.get();
//            if (activity == null) return;
//            if (activity.isFinished) return;
//            switch (msg.what) {
//                case CLPage.MSG_HTTP_RESPONSE:
//                    JSONObject jsonObject = (JSONObject) msg.obj;
//                    activity.pageLoaded(jsonObject);
//                    break;
//                case CLPage.MSG_HTTP_REQUEST_FAILED:
//                    activity.page.loadPage(activity.handler); // retry
//                    Log.d(TAG, "handleMessage: failed");
//                    break;
//            }
//        }
//    }

    //    MyHandler handler = new MyHandler(CLWebViewActivity.this);
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: current thread=" + Thread.currentThread().getId());
            switch (msg.what) {
                case CLPage.MSG_HTTP_RESPONSE:
                    JSONObject jsonObject = (JSONObject) msg.obj;
                    pageLoaded(jsonObject);
                    break;
                case CLPage.MSG_HTTP_REQUEST_FAILED:
                    page.loadPage(handler); // retry
                    Log.d(TAG, "handleMessage: failed");
                    break;
            }
            return true;
        }
    });


}
