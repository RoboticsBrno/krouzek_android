package com.example.tassadar.pocasi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ForecastActivity extends AppCompatActivity implements GetForecastTask.OnForecastLoadedListener {
    private Place mPlace;
    private ForecastAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mAdapter = new ForecastAdapter();

        RecyclerView list = findViewById(R.id.forecastList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(mAdapter);

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        if(pref.contains("latitude")) {
            double latitude = pref.getFloat("latitude", 0);
            double longitude = pref.getFloat("longitude", 0);
            Place p = Place.getClosest(getAssets(), latitude, longitude);
            setPlace(p);
        }

        refresh();
    }

    protected void onPause() {
        super.onPause();

        if(mPlace != null) {
            SharedPreferences pref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat("latitude", (float) mPlace.latitude);
            editor.putFloat("longitude", (float) mPlace.longitude);
            editor.apply();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_forecast, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.pickPlace:
                Intent i = new Intent(this, PlacesActivity.class);
                startActivityForResult(i, 0);
                return true;
            case R.id.refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        setPlace((Place)data.getSerializableExtra("place"));
        refresh();
    }

    private void refresh() {
        if(mPlace == null) {
            Intent i = new Intent(this, PlacesActivity.class);
            startActivityForResult(i, 0);
            return;
        }

        mAdapter.setList(null);

        String url = String.format(Locale.US,
                "http://aladin.spekacek.com/meteorgram/endpoint-v2/" +
                "getWeatherInfo?latitude=%f&longitude=%f", mPlace.latitude, mPlace.longitude);
        new GetForecastTask(this).execute(url);
    }

    private void setPlace(Place place) {
        mPlace = place;

        TextView placeText = findViewById(R.id.place);
        placeText.setText(place.name + ", " + place.area);
    }

    @Override
    public void onForecastLoaded(String forecastJson) {
        if (forecastJson == null) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage("Nepodařilo se načíst počasí!")
                .setPositiveButton("OK", null)
                .create().show();
            return;
        }

        try {
            JSONObject forecast = new JSONObject(forecastJson);

            String forecastTimeIso = forecast.getString("forecastTimeIso");
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
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage("Informace o počasí jsou moc staré!")
                        .setPositiveButton("OK", null)
                        .create().show();
                return;
            }

            long forecastMs = dateForecast.getTime();
            List<ForecastItem> items = new ArrayList<>();
            for(int i = nowIndex; i < temp.length(); ++i) {
                ForecastItem it = new ForecastItem();
                it.date = new Date(forecastMs + i*60*60*1000);
                it.temperature = temp.getDouble(i);
                items.add(it);
            }

            mAdapter.setList(items);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
