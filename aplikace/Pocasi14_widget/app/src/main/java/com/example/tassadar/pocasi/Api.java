package com.example.tassadar.pocasi;

import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Api {
    public static final String URL_TEMPLATE = "http://aladin.spekacek.com/meteorgram/endpoint-v2/" +
            "getWeatherInfo?latitude=%f&longitude=%f";

    public static final long PERIOD_MS = 6 * 60 * 60 * 1000;

    public static final DateFormat DATE_FORMAT;
    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Prague"));
    }

    public static List<ForecastItem> parse(String forecastJson) throws JSONException, ParseException {
        JSONObject forecast = new JSONObject(forecastJson);

        String forecastTimeIso = forecast.getString("forecastTimeIso");
        Date dateForecast = DATE_FORMAT.parse(forecastTimeIso);

        Date dateNow = new Date();

        int nowIndex = 0;
        if (dateNow.after(dateForecast)) {
            long diffMs = (dateNow.getTime() - dateForecast.getTime());
            nowIndex = (int) (diffMs / (3600 * 1000));
        }

        List<ForecastItem> items = new ArrayList<>();

        JSONObject params = forecast.getJSONObject("parameterValues");
        JSONArray temp = params.getJSONArray("TEMPERATURE");
        if (nowIndex > temp.length()) {
            return items;
        }

        JSONArray rain = params.getJSONArray("PRECIPITATION_TOTAL");
        JSONArray clouds = params.getJSONArray("CLOUDS_TOTAL");
        JSONArray wind = params.getJSONArray("WIND_SPEED");
        JSONArray pressure = params.getJSONArray("PRESSURE");
        JSONArray icons = forecast.getJSONArray("weatherIconNames");

        long forecastMs = dateForecast.getTime();
        for(int i = nowIndex; i < temp.length(); ++i) {
            ForecastItem it = new ForecastItem();
            it.date = new Date(forecastMs + i*60*60*1000);
            it.temperature = temp.getDouble(i);
            it.rain = rain.getDouble(i);
            it.clouds = clouds.getDouble(i);
            it.wind = wind.getDouble(i);
            it.pressure = pressure.getDouble(i);
            it.icon = icons.getString(i/2);
            items.add(it);
        }
        return items;
    }
}
