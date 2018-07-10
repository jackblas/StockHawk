package com.udacity.stockhawk.remote;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteEndpoint {

    private static final String LOG_TAG = "RemoteEndpoint";

    public static String fetchPlainText(URL url)
            throws IOException {

        String jsonString=null;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        jsonString = response.body().string();

        return jsonString;
    }
}
