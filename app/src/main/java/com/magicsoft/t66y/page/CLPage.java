package com.magicsoft.t66y.page;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.magicsoft.t66y.http.HttpRequest;
import com.magicsoft.t66y.util.Regex;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class CLPage {

    protected String url;
    private Context context;

    final public static int MSG_HTTP_RESPONSE = 10000;
    final public static int MSG_HTTP_REQUEST_FAILED = 11000;

    HandlerThread handlerThread;
    Handler handler;

    abstract void jsonParse(String response, final JSONObject json);
    public abstract String template();
    abstract protected String url();

    CLPage(String url, Context context) {
        this.url = url;
        this.context = context;
        initHandler();
    }

    void initHandler() {
        handlerThread = new HandlerThread(this.url());
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HttpRequest.DATA_ARRIVED:
                        String response = (String) msg.obj;
                        final JSONObject json = new JSONObject();
                        jsonParse(response, json);
                        Log.e("JSON", json.toString());
                        pageLoaded(json);
                }
            }
        };
    }

    public boolean same(CLPage page) {
        try {
            URI uriPage = new URI(page.url);
            URI uriThis = new URI(this.url);
            return uriPage.getPath().equals(uriThis.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static CLPage pageFromUrl(String url, Context context)
            throws PageClassNotExistsException {
        if (null != Regex.match_one("/index\\.php", url, 0)) {
            return new CLIndexPage(url, context);
        } else if (null != Regex.match_one("thread\\d+\\.php", url, 0)) {
            return new CLThreadPage(url, context);
        } else if (null != Regex.match_one("htm_data/\\d+/\\d+/", url, 0)) {
            return new CLReadPage(url, context);
        } else if (null != Regex.match_one("read\\.php\\?tid=\\d+", url, 0)) {
            return new CLReadPage(url, context);
        }
        throw new PageClassNotExistsException();
    }

    public static class PageClassNotExistsException extends Exception {
    }

//    public interface PageLoadDelegate {
//        void pageLoaded(JSONObject json);
//    }

    protected void pageLoaded(JSONObject json) {}

//    public void loadPage(final PageLoadDelegate delegate) {
//        final String url = this.url;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HttpRequest.httpGet(url, new HttpRequest.HttpDelegate() {
//
//                    @Override
//                    public void httpRequestDidReceived(String response) {
//                        final JSONObject json = new JSONObject();
//                        jsonParse(response, json);
//                        Log.e("JSON", json.toString());
//                        delegate.pageLoaded(json);
//                    }
//                }, "gbk");
//
//            }
//        }).start();
//    }

    public void loadPage(final Handler webViewHandler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = HttpRequest.httpGet(new URL(url), "gbk");
                    final JSONObject json = new JSONObject();
                    jsonParse(response, json);
                    Log.e("JSON", json.toString());
                    webViewHandler.obtainMessage(MSG_HTTP_RESPONSE, json).sendToTarget();
                    return;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                webViewHandler.obtainMessage(MSG_HTTP_REQUEST_FAILED, url).sendToTarget();
            }
        });

    }

    @Override
    protected void finalize() throws Throwable {
        handlerThread.quit();
        super.finalize();
    }
}
