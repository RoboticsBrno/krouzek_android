package com.example.tassadar.pocasi;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlacesAdapter extends ListAdapter<Place, PlacesAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnPlaceClickedListener {
        void OnPlaceClicked(Place p);
    }

    private OnPlaceClickedListener mListener;

    PlacesAdapter(OnPlaceClickedListener listener) {
        super(Place.DIFF_CALLBACK);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_places, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int pos) {
        final Place p = getItem(pos);
        TextView name = viewHolder.itemView.findViewById(R.id.placeName);
        name.setText(p.name);

        TextView area = viewHolder.itemView.findViewById(R.id.areaName);
        area.setText(p.area);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.OnPlaceClicked(p);
            }
        });
    }
}
