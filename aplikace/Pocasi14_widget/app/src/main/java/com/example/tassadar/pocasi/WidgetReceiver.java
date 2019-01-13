package com.example.tassadar.pocasi;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WidgetReceiver extends AppWidgetProvider {

    private static class SizeStop {
        SizeStop(int minWidth, int idDivider, int idCell) {
            this.minWidth = minWidth;
            this.idDivider = idDivider;
            this.idCell = idCell;
        }
        int minWidth;
        int idDivider;
        int idCell;
    }

    private static SizeStop[] mSizeStops = new SizeStop[] {
            new SizeStop(0, R.id.cell0, R.id.cell0),
            new SizeStop(200, R.id.divider0, R.id.cell1),
            new SizeStop(350, R.id.divider1, R.id.cell2),
    };

    private RemoteViews updateCell(Context ctx, ForecastItem it, boolean first) {
        RemoteViews cell = new RemoteViews(ctx.getPackageName(), R.layout.widget_forecast_cell);
        cell.setTextViewText(R.id.temperature, String.format("%.1f°", it.temperature));
        cell.setTextViewText(R.id.rain, String.format("%.1f mm/h", it.rain));
        cell.setTextViewText(R.id.wind, String.format("%.1f m/s", it.wind));

        Resources res = ctx.getResources();
        int iconId = res.getIdentifier(it.icon, "drawable", BuildConfig.APPLICATION_ID);
        cell.setImageViewResource(R.id.icon, iconId);

        if(!first) {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            cell.setTextViewText(R.id.time, df.format(it.date));
        }

        return cell;
    }

    private RemoteViews prepareViews(Context context, int minWidth, List<ForecastItem> forecast) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_forecast);

        Intent i = new Intent(context, ForecastActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.root, intent);

        if(forecast == null || forecast.size() == 0) {
            views.setTextViewText(R.id.status_text, "Nepodařilo se načíst předpověď!");
            views.setViewVisibility(R.id.status_text, View.VISIBLE);
            return views;
        } else {
            views.setViewVisibility(R.id.status_text, View.GONE);
        }

        int next = 0;
        for(SizeStop stop : mSizeStops) {
            views.removeAllViews(stop.idCell);
            if(minWidth >= stop.minWidth) {
                views.addView(stop.idCell, updateCell(context, forecast.get(next), next == 0));
                views.setViewVisibility(stop.idDivider, View.VISIBLE);
                views.setViewVisibility(stop.idCell, View.VISIBLE);
            } else {
                views.setViewVisibility(stop.idCell, View.GONE);
                views.setViewVisibility(stop.idDivider, View.GONE);
            }

            next = Math.min(forecast.size()-1, next + 6);
        }

        return views;
    }

    private List<ForecastItem> parseForecast(String forecastJson) {
        if(forecastJson == null || forecastJson.isEmpty()) {
            return null;
        }

        try {
            return Api.parse(forecastJson);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        SharedPreferences pref = context.getSharedPreferences("", Context.MODE_PRIVATE);
        List<ForecastItem> forecast = parseForecast(pref.getString("forecastJson", ""));
        RemoteViews views = prepareViews(context, newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH), forecast);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("WidgetReceiver", "update");

        SharedPreferences pref = context.getSharedPreferences("", Context.MODE_PRIVATE);

        long nextForecast = pref.getLong("forecastTime", 0);
        nextForecast += (Api.PERIOD_MS - (nextForecast%(Api.PERIOD_MS))) + 60*1000;
        if(new Date().getTime() >= nextForecast) {
            Log.i("WidgetReceiver", "update forecast");
            Intent i = new Intent(context, WidgetService.class);
            i.putExtra("appWidgetIds", appWidgetIds);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            } else {
                context.startService(i);
            }
        }

        if(!pref.contains("forecastJson")) {
            return;
        }

        List<ForecastItem> forecast = parseForecast(pref.getString("forecastJson", ""));
        for(int id : appWidgetIds) {
            int width = 110;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Bundle options = appWidgetManager.getAppWidgetOptions(id);
                width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            }
            appWidgetManager.updateAppWidget(id, prepareViews(context, width, forecast));
        }
    }
}
