package com.magicsoft.t66y.page;

import android.content.Context;

import com.magicsoft.t66y.EntryActivity;
import com.magicsoft.t66y.util.Regex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;

public class CLIndexPage extends CLPage {

    CLIndexPage(String url, Context context) {
        super(url, context);
    }

    @Override
    protected String url() {
        return url;
    }

    @Override
    void jsonParse(String response, JSONObject json) {
        String title = Regex.match_one("<title>(.*?)</title>", response, 1);
        String pattern;
        pattern = "<tr class=\"tr3 f_one\">[\\s\\S]*?"
                + "<h2><a.*?href=\"(.*?)\">(.*?)</a></h2>[\\s\\S]*?"
                + "<br /><span class=\"smalltxt gray\">(.*?)</span>[\\s\\S]*?"
                + "</tr>";
        Matcher m = Regex.match(pattern, response);

        try {
            json.put("title", title);
            JSONArray threads = new JSONArray();
            while (m.find()) {
                JSONObject thread = new JSONObject();
                thread.put("link", EntryActivity.DomainName() + m.group(1));
                thread.put("title", m.group(2));
                thread.put("text", m.group(3));
                threads.put(thread);
            }
            json.put("threads", threads);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String template() {
        return "file:///android_asset/page.html";
    }
}

