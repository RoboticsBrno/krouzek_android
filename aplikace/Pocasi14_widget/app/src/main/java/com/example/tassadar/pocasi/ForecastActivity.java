package com.example.tassadar.pocasi;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

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
    private FusedLocationProviderClient mLocation;
    private boolean mAutoGps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        View header = findViewById(R.id.forecastHeader);
        ((TextView)header.findViewById(R.id.date)).setText("Čas");
        ((TextView)header.findViewById(R.id.temperature)).setText("Teplota");
        ((TextView)header.findViewById(R.id.rain)).setText("Déšť");
        header.findViewById(R.id.icon).setBackgroundColor(0);
        ((TextView)header.findViewById(R.id.wind)).setText("Vítr");
        ((TextView)header.findViewById(R.id.pressure)).setText("Tlak");

        mLocation = LocationServices.getFusedLocationProviderClient(this);
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

        mAutoGps = pref.getBoolean("autoGps", false);
        if(mAutoGps) {
            getCurrentLocation();
        } else {
            refresh();
        }
    }

    protected void onPause() {
        super.onPause();

        if(mPlace != null) {
            SharedPreferences pref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat("latitude", (float) mPlace.latitude);
            editor.putFloat("longitude", (float) mPlace.longitude);
            editor.putBoolean("autoGps", mAutoGps);
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
                if(mAutoGps) {
                    getCurrentLocation();
                } else {
                    refresh();
                }
                return true;
            case R.id.gps:
                mAutoGps = true;
                getCurrentLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        mAutoGps = false;
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

        String text = place.name + ", " + place.area;
        if(mAutoGps) {
            text = "*" + text;
        }

        TextView placeText = findViewById(R.id.place);
        placeText.setText(text);
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

            JSONArray rain = params.getJSONArray("PRECIPITATION_TOTAL");
            JSONArray clouds = params.getJSONArray("CLOUDS_TOTAL");
            JSONArray wind = params.getJSONArray("WIND_SPEED");
            JSONArray pressure = params.getJSONArray("PRESSURE");

            JSONArray icons = forecast.getJSONArray("weatherIconNames");

            long forecastMs = dateForecast.getTime();
            List<ForecastItem> items = new ArrayList<>();
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

            mAdapter.setList(items);
            fillHeader(items);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final LocationCallback mLocCallback = new LocationCallback() {
        public void onLocationResult (LocationResult result) {
            Location loc = result.getLastLocation();
            if(loc == null) {
                Toast.makeText(ForecastActivity.this, "Nepodařilo se získat pozici z GPS!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Place closest = Place.getClosest(getAssets(), loc.getLatitude(), loc.getLongitude());
            setPlace(closest);
            refresh();
        }
    };

    private void getCurrentLocation() {
        int c = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (c != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            }, 0);
            return;
        }

        Toast.makeText(this, "Hledám polohu pomocí GPS...", Toast.LENGTH_SHORT).show();

        LocationRequest req = new LocationRequest();
        req.setExpirationDuration(30000);
        req.setNumUpdates(1);
        req.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocation.requestLocationUpdates(req, mLocCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.getCurrentLocation();
        }
    }

    private void fillHeader(List<ForecastItem> items) {
        double temp = 0;
        double rain = 0;
        double wind = 0;
        double clouds = 0;

        int count = Math.min(12, items.size());
        if(count == 0)
            return;

        for(int i = 0; i < count; ++i) {
            ForecastItem it = items.get(i);
            temp += it.temperature;
            rain += it.rain;
            wind += it.wind;
            clouds += it.clouds;
        }

        TextView t = findViewById(R.id.headerTemp);
        t.setText(String.format("%.1f°", temp/count));

        t = findViewById(R.id.headerRain);
        t.setText(String.format("%.1f mm/%dh", rain, count));

        t = findViewById(R.id.headerWind);
        t.setText(String.format("%.1f m/s", wind/count));

        t = findViewById(R.id.headerClouds);
        t.setText(String.format("%.0f %%", (clouds/count)*100));
    }
}
