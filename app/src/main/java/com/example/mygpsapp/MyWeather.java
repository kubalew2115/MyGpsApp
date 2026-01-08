package com.example.mygpsapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;

public class MyWeather extends BaseActivity {

    private final String CITY = "Gdańsk,pl";
    private final String MY_API = "8b97fdefdc68b6a207e07fd1b957719d";

    private TextView cityTextView;
    private TextView detailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_weather);

        initNavigationRail();

        setNavigationItemSelected(R.id.nav_weather);

        cityTextView = findViewById(R.id.weather_city_name);
        detailsTextView = findViewById(R.id.weather_details);

        new FetchWeatherTask().execute();
    }

    @Override
    protected void onFabClicked() {
        new FetchWeatherTask().execute();
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... args) {
            String targetURL = "https://api.openweathermap.org/data/2.5/weather?q="
                    + CITY + "&units=metric&appid=" + MY_API;
            return HttpRequest.excuteGet(targetURL);
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) return;
            try {
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.has("cod") && jsonObj.getInt("cod") != 200) return;

                JSONObject main = jsonObj.getJSONObject("main");
                String cityName = jsonObj.getString("name");
                double temp = main.getDouble("temp");
                double pressure = main.getDouble("pressure");

                cityTextView.setText(cityName);
                String weatherDetails = String.format(Locale.getDefault(),
                        "Temperatura: %.1f°C\nCiśnienie: %.0f hPa\n",
                        temp, pressure);
                detailsTextView.setText(weatherDetails);

            } catch (Exception e) {
            }
        }
    }
}