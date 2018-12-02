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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Place implements Serializable {
    String name;
    String area;
    double latitude;
    double longitude;

    double distanceTo(double lat, double lon) {
        return Math.sqrt(
                Math.pow(lat - this.latitude, 2) +
                Math.pow(lon - this.longitude, 2)
        );
    }

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

    static Place getClosest(AssetManager assets, double lat, double lon) {
        List<Place> places = getAll(assets);

        Place closest = places.get(0);
        double closestDist = closest.distanceTo(lat, lon);
        for(Place p : places) {
            double curDist = p.distanceTo(lat, lon);
            if(curDist < closestDist) {
                closest = p;
                closestDist = curDist;
            }
        }
        return closest;
    }
}
