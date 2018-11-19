package com.example.tassadar.pocasi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class PlacesActivity extends AppCompatActivity implements PlacesAdapter.OnPlaceClickedListener {
    private PlacesAdapter mAdapter;
    private ArrayList<Place> mPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        RecyclerView list = findViewById(R.id.placesList);
        list.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new PlacesAdapter(this);
        list.setAdapter(mAdapter);

        loadPlaces();
    }

    private void loadPlaces() {
        mPlaces = new ArrayList<>();

        try {
            InputStream in = getAssets().open("places.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while(true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                String[] cols = line.split(";");

                Place p = new Place();
                p.name = cols[0];
                p.area = cols[3];
                p.latitude = Double.valueOf(cols[1]);
                p.longitude = Double.valueOf(cols[2]);
                mPlaces.add(p);
            }
            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mAdapter.submitList(mPlaces);
    }

    @Override
    public void OnPlaceClicked(Place p) {
        Intent data = new Intent();
        data.putExtra("name", p.name);
        data.putExtra("area", p.area);
        data.putExtra("latitude", p.latitude);
        data.putExtra("longitude", p.longitude);
        setResult(RESULT_OK, data);
        finish();
    }
}
