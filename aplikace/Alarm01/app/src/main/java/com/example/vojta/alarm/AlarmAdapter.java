package com.example.vojta.alarm;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private List<Alarm> mAlarms;

    public AlarmAdapter(List<Alarm> alarms) {
        mAlarms = alarms;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_alarm, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Alarm a = mAlarms.get(i);

        TextView time = viewHolder.itemView.findViewById(R.id.time);
        time.setText(a.getTimeText());

        Switch enabled = viewHolder.itemView.findViewById(R.id.enabled);
        enabled.setChecked(a.enabled);
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
    }
}
