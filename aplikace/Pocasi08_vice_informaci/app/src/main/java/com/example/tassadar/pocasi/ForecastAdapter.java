package com.example.tassadar.pocasi;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private List<ForecastItem> mForecast;
    private SimpleDateFormat mDateFormat;

    ForecastAdapter() {
        super();
        mDateFormat = new SimpleDateFormat("EE\nHH:mm");
    }

    public void setList(List<ForecastItem> list) {
        mForecast = list;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return mForecast != null ? mForecast.size() : 0;
    }

    public ViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_forecast, viewGroup, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int pos) {
        ForecastItem it = mForecast.get(pos);

        TextView temp = viewHolder.itemView.findViewById(R.id.temperature);
        temp.setText(String.format("%5.1f°", it.temperature));

        TextView date = viewHolder.itemView.findViewById(R.id.date);
        date.setText(mDateFormat.format(it.date));

        TextView rain = viewHolder.itemView.findViewById(R.id.rain);
        rain.setText(String.format("%.1f mm/h", it.rain));

        TextView clouds = viewHolder.itemView.findViewById(R.id.clouds);
        clouds.setText(String.format("%.0f %%", it.clouds*100));

        TextView wind = viewHolder.itemView.findViewById(R.id.wind);
        wind.setText(String.format("%.1f\nm/s", it.wind));

        TextView pressure = viewHolder.itemView.findViewById(R.id.pressure);
        pressure.setText(String.format("%.1f hPa", it.pressure / 100));
    }
}
