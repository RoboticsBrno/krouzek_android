package com.example.tassadar.pocasi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

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

        mPlaces = Place.getAll(getAssets());
        mAdapter.submitList(mPlaces);

        EditText hledat = findViewById(R.id.searchText);
        hledat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
    }

    private void filter(String filter) {
        ArrayList<Place> filtered;

        filter = filter.trim().toUpperCase();
        if(filter.isEmpty()) {
           filtered = mPlaces;
        } else {
            filtered = new ArrayList<>();
            for (Place p : mPlaces) {
                if (p.name.toUpperCase().contains(filter)) {
                    filtered.add(p);
                }
            }
        }

        RecyclerView list = findViewById(R.id.placesList);
        mAdapter = new PlacesAdapter(this);
        list.setAdapter(mAdapter);
        mAdapter.submitList(filtered);
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
