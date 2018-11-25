package com.example.tassadar.pocasi;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Place {
    String name;
    String area;
    double latitude;
    double longitude;

    static final DiffUtil.ItemCallback<Place> DIFF_CALLBACK = new DiffUtil.ItemCallback<Place>() {
        @Override
        public boolean areItemsTheSame(Place place, Place t1) {
            return place.latitude == t1.latitude &&
                    place.longitude == t1.longitude &&
                    place.name.equals(t1.name);
        }

        @Override
        public boolean areContentsTheSame(Place place, Place t1) {
            return areItemsTheSame(place, t1);
        }
    };

    static ArrayList<Place> getAll(AssetManager assets) {
        ArrayList<Place> result = new ArrayList<>();

        try {
            InputStream in = assets.open("places.csv");
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
                result.add(p);
            }
            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
