package com.example.mygpsapp;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpRequest {

    private static final OkHttpClient client = new OkHttpClient();

    public static String excuteGet(String targetURL) {
        Request request = new Request.Builder()
                .url(targetURL)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.e("HttpRequest_Error", "Nieudane zapytanie. Kod: " + response.code());
                return null;
            }

            ResponseBody body = response.body();
            if (body != null) {
                return body.string();
            } else {
                Log.e("HttpRequest_Error", "Ciało odpowiedzi jest puste.");
                return null;
            }

        } catch (Exception e) {
            Log.e("HttpRequest_Exception", "Błąd podczas wykonywania zapytania GET", e);
            return null;
        }
    }
}
