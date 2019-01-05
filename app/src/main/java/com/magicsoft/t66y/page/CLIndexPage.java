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
                thread.put("link", EntryActivity.DomainName + m.group(1));
                thread.put("title", m.group(2));
                thread.put("text", m.group(3));
                threads.put(thread);
            }
            json.put("threads", threads);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /*
        @Override
        public JSONObject loadPage() {
            JSONObject json = new JSONObject();
            try {
                InputStream is = context.getAssets().open("html/index.html");
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
        return "file:///android_asset/page.html";
    }
}

