package com.magicsoft.t66y.page;

import android.content.Context;
import android.util.Log;

import com.magicsoft.t66y.http.HttpRequest;
import com.magicsoft.t66y.util.Regex;

import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class CLPage extends Object {

    public static final String HOSTNAME = "https://cl.0qc.info/";

    protected String url;
    private Context context;

    abstract void jsonParse(String response, final JSONObject json);
    public abstract String template();

    CLPage(String url, Context context) {
        this.url = url;
        this.context = context;
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

    public static class PageClassNotExistsException extends Exception {}

    public interface PageLoadDelegate {
        void pageLoaded(JSONObject json);
    }

    public void loadPage(final PageLoadDelegate delegate) {

        HttpRequest.httpGet(this.url, new HttpRequest.HttpDelegate() {

            @Override
            public void httpRequestDidReceived(String response) {
                final JSONObject json = new JSONObject();
                jsonParse(response, json);
                Log.e("JSON", json.toString());
                delegate.pageLoaded(json);
            }
        }, "gbk");

    }

}
