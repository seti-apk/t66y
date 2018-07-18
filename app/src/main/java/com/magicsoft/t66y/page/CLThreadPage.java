package com.magicsoft.t66y.page;

import android.content.Context;
import android.util.Log;

import com.magicsoft.t66y.http.URLParser;
import com.magicsoft.t66y.util.Regex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.regex.Matcher;

public class CLThreadPage extends CLPage {

    private String page;

    CLThreadPage(String url, Context context) {
        super(url, context);
        try {
            URLParser parser = new URLParser(url);
            page = parser.param("page");
            if (null == page) {
                page = "1";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    void jsonParse(String response, JSONObject json) {
        String title = Regex.match_one("<title>(.*?)</title>", response, 1);
        String pattern;
        pattern = "<td class=\"tal\".*?>[\\s\\S]*?"
                + "<h3><a.*?href=\"(.*?)\".*?>(.*?)</a>";
        Matcher m = Regex.match(pattern, response);

        // prev page & next page
        try {
            Log.e("PAGE", url);
            URLParser parser = new URLParser(this.url);
            int iPage = Integer.valueOf(page);
            // prev page
            if (iPage > 1) {
                parser.param("page", String.valueOf(iPage - 1));
                json.put("prevpage", parser.toString());
            }
            // next page
            parser.param("page", String.valueOf(iPage + 1));
            json.put("nextpage", parser.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            json.put("page", this.page);
            json.put("title", title);
            JSONArray reads = new JSONArray();
            while (m.find()) {
                JSONObject read = new JSONObject();
                read.put("link", HOSTNAME + m.group(1));
                read.put("title", m.group(2));
                reads.put(read);
            }
            json.put("reads", reads);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
/*
    @Override
    public JSONObject loadPage() {
        JSONObject json = new JSONObject();
        try {
            InputStream is = context.getAssets().open("html/thread.html");
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            String response = new String(buffer, "gbk");
            jsonParse(response, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
//*/
    @Override
    public String template() {
        return "file:///android_asset/thread.html";
    }
}
