package com.magicsoft.t66y.page;

import android.content.Context;
import com.magicsoft.t66y.util.Regex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;

public class CLReadPage extends CLPage {

    CLReadPage(String url, Context context) {
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
        pattern = "<div class=\"tpc_content do_not_catch\">([\\s\\S]*?)</div>\\s*</td></tr>";
        Matcher m_content = Regex.match(pattern, response);
        String h4 = Regex.match_one("<h4>(.*?)</h4>", response, 1);
        try {
            json.put("title", title);
            json.put("h4", h4);
            JSONArray contents = new JSONArray();
            while (m_content.find()) {
                String sContent = m_content.group(1);
                sContent = sContent.replaceAll("<table.*?>", "");
                sContent = sContent.replaceAll("<t[hrd].*?>", "");
                sContent = sContent.replaceAll("</t[hrd]>", "");
                JSONObject content = new JSONObject();
                content.put("content", sContent);
                contents.put(content);
            }
            json.put("contents", contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String template() {
        return "file:///android_asset/read.html";
    }
}
