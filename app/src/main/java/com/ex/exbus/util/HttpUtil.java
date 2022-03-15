package com.ex.exbus.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;

public class HttpUtil {

    public interface HttpCallback {
        public void onResponse(String response);
    }

    public static void request(final Context context, final String endpoint, final HttpCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(endpoint);
                    Log.i("HttpUtil", "url : "+endpoint);
                    conn = (HttpURLConnection) url.openConnection();

                    conn.setUseCaches(false);
                    conn.setRequestMethod("GET");

                    String cookie = getHttpClientCookiesFromWebKit(context, endpoint);
                    conn.setRequestProperty("Cookie", cookie);
                    conn.setDoOutput(true);
                    OutputStream out = conn.getOutputStream();
                    out.close();

                    // Handle the response.
                    int status = conn.getResponseCode();
                    if (status != HttpURLConnection.HTTP_OK) {
                        callback.onResponse(null);
                    } else {
                        InputStream in = conn.getInputStream();
                        ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        for (int n; (n = in.read(buf)) != -1; ) {
                            bufStream.write(buf, 0, n);
                        }
                        in.close();
                        final String responseString = new String(bufStream.toByteArray(), "utf-8");
                        bufStream.close();

                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(responseString);
                            }
                        };
                        mainHandler.post(myRunnable);
                    }
                } catch (Exception e) {
                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(null);
                        }
                    };
                    mainHandler.post(myRunnable);
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    public static String getHttpClientCookiesFromWebKit(Context context, String url) throws Exception {

        CookieManager cookieManager = CookieManager.getInstance();
        URL parsedURL = new URL(url);
        String domain = parsedURL.getHost();
        String urlCookieString = cookieManager.getCookie(domain);
        return urlCookieString;
    }
}