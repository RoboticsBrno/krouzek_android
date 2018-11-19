package com.example.tassadar.pocasi;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class Place {
    public String name;
    public String area;
    public double latitude;
    public double longitude;

    static final DiffUtil.ItemCallback<Place> DIFF_CALLBACK = new DiffUtil.ItemCallback<Place>() {
        @Override
        public boolean areItemsTheSame(Place place, Place t1) {
            return place.latitude == t1.latitude &&
                    place.longitude == t1.longitude;
        }

        @Override
        public boolean areContentsTheSame(Place place, Place t1) {
            return place.name.equals(t1.name);
        }
    };
}
