package com.example.tassadar.pocasi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
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

        Button jakJe = findViewById(R.id.jakJeButton);
        jakJe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://aladin.spekacek.com/meteorgram/endpoint-v2/getWeatherInfo?latitude=49.2030&longitude=16.5976";
                GetForecastTask task = new GetForecastTask(MainActivity.this);
                task.execute(url);
            }
        });
    }

    public void onForecastLoaded(String forecastJson) {
        Log.i("Pocasi", "Nacteno! " + forecastJson);

        JSONObject forecast = null;
        try {
            forecast = new JSONObject(forecastJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
