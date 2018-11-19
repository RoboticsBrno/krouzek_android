package com.example.tassadar.pocasi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText latView = findViewById(R.id.latitude);
        EditText lonView = findViewById(R.id.longitude);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        latView.setText(pref.getString("latitude", "49.2030"));
        lonView.setText(pref.getString("longitude", "16.5976"));

        Button jakJe = findViewById(R.id.jakJeButton);
        jakJe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText latView = findViewById(R.id.latitude);
                EditText lonView = findViewById(R.id.longitude);

                double latitude, longitude;
                try {
                    latitude = Double.valueOf(latView.getText().toString());
                    longitude = Double.valueOf(lonView.getText().toString());
                } catch (NumberFormatException e) {
                    TextView txt = findViewById(R.id.pocasiText);
                    txt.setText("Špatný formát souřadnic.");
                    return;
                }

                String url = String.format("http://aladin.spekacek.com/meteorgram/endpoint-v2/" +
                        "getWeatherInfo?latitude=%f&longitude=%f", latitude, longitude);
                GetForecastTask task = new GetForecastTask(MainActivity.this);
                task.execute(url);
            }
        });
    }

    protected void onPause() {
        super.onPause();

        EditText latView = findViewById(R.id.latitude);
        EditText lonView = findViewById(R.id.longitude);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("latitude", latView.getText().toString());
        editor.putString("longitude", lonView.getText().toString());
        editor.apply();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.pickPlace:
                Intent i = new Intent(this, PlacesActivity.class);
                startActivityForResult(i, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }

        EditText latView = findViewById(R.id.latitude);
        EditText lonView = findViewById(R.id.longitude);

        latView.setText(String.valueOf(data.getDoubleExtra("latitude", 0)));
        lonView.setText(String.valueOf(data.getDoubleExtra("longitude", 0)));
    }

    public void onForecastLoaded(String forecastJson) {
        Log.i("Pocasi", "Nacteno! " + forecastJson);

        TextView txt = (TextView) findViewById(R.id.pocasiText);
        if (forecastJson == null) {
            txt.setText("Nepodařilo se načíst počasí!");
            return;
        }

        try {
            JSONObject forecast = new JSONObject(forecastJson);

            String forecastTimeIso = forecast.getString("forecastTimeIso");
            Log.i("Pocasi", "Predpoved vygenerovana v " + forecastTimeIso);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("Europe/Prague"));
            Date dateForecast = df.parse(forecastTimeIso);

            Date dateNow = new Date();

            int nowIndex = 0;
            if (dateNow.after(dateForecast)) {
                long diffMs = (dateNow.getTime() - dateForecast.getTime());
                nowIndex = (int) (diffMs / (3600 * 1000));
            }

            JSONObject params = forecast.getJSONObject("parameterValues");
            JSONArray temp = params.getJSONArray("TEMPERATURE");
            if (nowIndex > temp.length()) {
                txt.setText("Informace o počasí jsou moc staré a neobsahují aktuální hodinu :(");
                return;
            }

            Date roundedHour = new Date(dateForecast.getTime() + nowIndex * 3600 * 1000);
            txt.setText(String.format("Teplota v %s je %.1f °C",
                    df.format(roundedHour), temp.getDouble(nowIndex)));
        } catch (JSONException ex) {
            ex.printStackTrace();
            txt.setText("Špatný formát dat o počasí!");
        } catch (ParseException ex) {
            ex.printStackTrace();
            txt.setText("Špatný formát času!");
        }
    }
}
