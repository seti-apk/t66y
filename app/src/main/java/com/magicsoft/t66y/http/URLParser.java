package com.magicsoft.t66y.http;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class URLParser {

    private URL url;
    private HashMap<String, String> kv = new HashMap<>();

    public URLParser(String url) throws MalformedURLException {
        this.url = new URL(url);
        String query = this.url.getQuery();
        String[] kvPairs = query.split("&");
        for (String pair : kvPairs) {
            Log.e("PAIR", url);
            Log.e("PAIR", pair);
            String[] param = pair.split("=");
            String k = param[0];
            String v = param[1];
            kv.put(k, v);
        }
    }

    public void param(String k, String v) {
        kv.put(k, v);
    }

    public String param(String k) {
        return kv.get(k);
    }

    public String toString() {
        ArrayList<String> kvPairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : kv.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            kvPairs.add(k + "=" + v);
        }
        String params = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params = String.join("&", kvPairs);
        } else {
            return null;
        }

        URI newURI;
        try {
            URI uri = url.toURI();
            newURI = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    params,
                    uri.getFragment()
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        return newURI.toString();
    }
}
