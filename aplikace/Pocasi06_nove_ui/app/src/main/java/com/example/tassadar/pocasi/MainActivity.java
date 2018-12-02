package com.example.tassadar.pocasi;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements GetForecastTask.OnForecastLoadedListener {
    FusedLocationProviderClient mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, ForecastActivity.class));
        finish();

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

                updatePlaceName(latitude, longitude);

                String url = String.format(Locale.US, "http://aladin.spekacek.com/meteorgram/endpoint-v2/" +
                        "getWeatherInfo?latitude=%f&longitude=%f", latitude, longitude);
                GetForecastTask task = new GetForecastTask(MainActivity.this);
                task.execute(url);
            }
        });

        mLocation = LocationServices.getFusedLocationProviderClient(this);
        Button gpsBtn = findViewById(R.id.gpsBtn);
        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
            }
        });

        TextView place = findViewById(R.id.place);
        place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText latView = findViewById(R.id.latitude);
                EditText lonView = findViewById(R.id.longitude);

                Uri uri = Uri.parse(String.format("geo:0,0?q=%s,%s", latView.getText(), lonView.getText()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
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

        double lat = data.getDoubleExtra("latitude", 0);
        double lon = data.getDoubleExtra("longitude", 0);

        updatePlaceName(lat, lon);

        latView.setText(String.valueOf(lat));
        lonView.setText(String.valueOf(lon));
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

    private void getCurrentLocation() {
        int c = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (c != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            }, 0);
            return;
        }

        final TextView txt = (TextView) findViewById(R.id.pocasiText);
        txt.setText("Hledám pozici pomocí GPS.");
        mLocation.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location == null) {
                    txt.setText("Nelze použít GPS");
                    return;
                }

                updatePlaceName(location.getLatitude(), location.getLongitude());

                EditText latView = findViewById(R.id.latitude);
                EditText lonView = findViewById(R.id.longitude);
                latView.setText(String.valueOf(location.getLatitude()));
                lonView.setText(String.valueOf(location.getLongitude()));
                txt.setText("");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.getCurrentLocation();
        }
    }

    private void updatePlaceName(double lat, double lon) {
        Place closest = Place.getClosest(getAssets(), lat, lon);

        TextView placeInfo = findViewById(R.id.place);
        placeInfo.setText(closest.name + ", " + closest.area);
    }
}
