package com.magicsoft.t66y;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.MeasureSpec;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.magicsoft.t66y.page.CLPage;

import org.json.JSONException;
import org.json.JSONObject;

public class CLWebViewActivity extends AppCompatActivity
        implements CLPage.PageLoadDelegate{

    private CLWebViewActivity ui = this;
    private JSONObject json;
    boolean jsonLoaded = false;
    private WebView webView;
    private CLPage page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clweb_view);

        Log.e("ACT", String.valueOf(this.getTaskId()));
        Log.e("ACT", String.valueOf(this.hashCode()));
        Intent intent = getIntent();
        final String url = intent.getStringExtra("url");
        Log.w("url", url);

        webView = findViewById(R.id.webView);
        initWebView(webView);
//        loadUrl(url);

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case R.id.save:
                saveWebviewToLocalImage();
                break;
        }
        return true;
    }

    void saveWebviewToLocalImage() {
        webView.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        Bitmap longImage = Bitmap.createBitmap(webView.getMeasuredWidth(),
                webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(longImage);  // 画布的宽高和 WebView 的网页保持一致
        Paint paint = new Paint();
        canvas.drawBitmap(longImage, 0, webView.getMeasuredHeight(), paint);
        webView.draw(canvas);


    }

    void loadUrl(String url) {
        try {
            jsonLoaded = false;
            page = CLPage.pageFromUrl(url, this);
            webView.loadUrl(page.template());
            page.loadPage(this);
        } catch (CLPage.PageClassNotExistsException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("activity","active stop" + this.hashCode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("activity", "active resume" + this.hashCode());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("activity", "active destroy" + this.hashCode());
        webView.stopLoading();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("activity", "active start" + this.hashCode());
    }

    @Override
    public void pageLoaded(JSONObject json) {
        this.json = json;
        jsonLoaded = true;
        synchronized (webViewClient) {
            webViewClient.notifyAll();
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

    public class JSInterface {

        @JavascriptInterface
        public void showImage(String src) {
            Intent intent = new Intent(ui, ImageViewActivity.class);
            intent.putExtra("src", src);
            ui.startActivity(intent);
        }
    }

    final WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            ui.setTitle("加载中……");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            synchronized (this) {
                try {
                    if (!jsonLoaded) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d("PAGE FINISHED JSON", ui.json.toString());
            try {
                String title = ui.json.getString("title");
                title = title.replace("&nbsp;", " ");
                ui.setTitle(title);
                view.loadUrl("javascript:setJson("
                        + ui.json.toString()
                        + ")");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
            handler.proceed(); // 接受网站证书
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("URL", url);

            try {
                CLPage page = CLPage.pageFromUrl(url, null);
                if (page.same(ui.page)) {
                    Log.e("PAGE", "equals");
                    loadUrl(url);
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

}
