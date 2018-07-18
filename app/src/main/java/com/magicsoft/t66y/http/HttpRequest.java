package com.magicsoft.t66y.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpRequest {

    public interface HttpDelegate {
        void httpRequestDidReceived(String response);
    }

    public static void httpGet(final String url,
                               final HttpRequest.HttpDelegate delegate,
                               final String charsetName) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        URL _url = null;
                        try {
                            _url = new URL(url);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        HttpURLConnection conn;

                        try {
                            conn = (HttpURLConnection)_url.openConnection();
                            conn.setConnectTimeout(3000);
                            conn.setDoInput(true);
                            conn.setRequestMethod("GET");
                            InputStream is = conn.getInputStream();
                            InputStreamReader reader = new InputStreamReader(is, charsetName);

                            BufferedReader bufferedReader = new BufferedReader(reader);
                            StringBuffer buffer = new StringBuffer();
                            String temp = null;

                            while ((temp = bufferedReader.readLine()) != null) {
                                //取水--如果不为空就一直取
                                buffer.append(temp);
                            }
                            bufferedReader.close();
                            reader.close();
                            is.close();
                            conn.disconnect();
                            delegate.httpRequestDidReceived(buffer.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
    }

}
