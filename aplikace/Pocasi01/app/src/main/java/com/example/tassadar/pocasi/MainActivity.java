package com.example.tassadar.pocasi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onJakJeClick(View btn) {
        GetForecastTask task = new GetForecastTask(this);
        task.execute(new Pair<>(49.14402, 16.66766));
    }

    public void onForecastLoaded(JSONObject forecast) {
        Log.i("Pocasi", "Nacteno! " + forecast);

        TextView txt = (TextView)findViewById(R.id.pocasiText);
        if(forecast == null) {
            txt.setText("Nepodařilo se načíst počasí!");
            return;
        }

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("Europe/Prague"));
            Date dateForecast = df.parse(forecast.getString("forecastTimeIso"));
            Date dateNow = new Date();
            Log.i("Pocasi", "" + dateNow + "" + dateForecast);

            int nowIndex = 0;
            if(dateNow.after(dateForecast)) {
                long diff = (dateNow.getTime() - dateForecast.getTime());
                nowIndex = (int) (diff / (3600 * 1000));
            }

            JSONObject params = forecast.getJSONObject("parameterValues");
            JSONArray temp = params.getJSONArray("TEMPERATURE");
            if(nowIndex > temp.length()) {
                txt.setText("Informace o počasí jsou moc staré a neobsahují aktuální hodinu :(");
                return;
            }

            Date roundedHour = new Date(dateNow.getTime() - (dateNow.getTime() % (3600 * 1000)));
            txt.setText(String.format("Teplota v %s je %.1f \u00B0C",
                    df.format(roundedHour), temp.getDouble(nowIndex)));
        } catch(JSONException ex) {
            ex.printStackTrace();
            txt.setText("Špatný formát dat o počasí!");
        } catch(ParseException ex) {
            ex.printStackTrace();
            txt.setText("Špatný formát času!");
        }
    }
}
