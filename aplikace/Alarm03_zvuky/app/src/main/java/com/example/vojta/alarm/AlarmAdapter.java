package com.example.vojta.alarm;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsProvider;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.dpro.widgets.OnWeekdaysChangeListener;
import com.dpro.widgets.WeekdaysPicker;

import java.util.Calendar;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnAlarmChangedListener {
        void onAlarmChanged(Alarm a);
        void pickRingtone(Alarm a, int position);
        void pickSoundFile(Alarm a, int position);
    }

    private List<Alarm> mAlarms;
    private AlarmItem mExpandedItem;
    private OnAlarmChangedListener mChangeListener;

    public AlarmAdapter(List<Alarm> alarms, OnAlarmChangedListener listener) {
        mAlarms = alarms;
        mChangeListener = listener;
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

        AlarmItem it = new AlarmItem(i, a, viewHolder.itemView);
        viewHolder.itemView.setOnClickListener(it);

        TextView time = viewHolder.itemView.findViewById(R.id.time);
        time.setText(a.getTimeText());
        time.setOnClickListener(it);

        Switch enabled = viewHolder.itemView.findViewById(R.id.enabled);
        enabled.setChecked(a.enabled);
        enabled.setOnCheckedChangeListener(it);

        ImageButton expBtn = it.mRoot.findViewById(R.id.expand_btn);
        expBtn.setOnClickListener(it);

        CheckBox vibrate = it.mRoot.findViewById(R.id.box_vibrate);
        vibrate.setChecked(a.vibrate);
        vibrate.setOnCheckedChangeListener(it);

        Button delete = it.mRoot.findViewById(R.id.btn_delete);
        delete.setOnClickListener(it);

        Button sound = it.mRoot.findViewById(R.id.btn_sound);
        sound.setOnClickListener(it);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ImageButton sound_file = it.mRoot.findViewById(R.id.btn_sound_file);
            sound_file.setVisibility(View.VISIBLE);
            sound_file.setOnClickListener(it);
        }

        List<Integer> daysList = a.getSelectedDays();

        RadioGroup repeat = it.mRoot.findViewById(R.id.repeat);
        if(a.hour == Alarm.HOUR_ANY) {
            repeat.check(R.id.repeat_hour);
        } else if(daysList.size() != 7) {
            repeat.check(R.id.repeat_days);
        } else {
            repeat.check(R.id.repeat_none);
        }
        repeat.setOnCheckedChangeListener(it);

        WeekdaysPicker days = it.mRoot.findViewById(R.id.days);
        days.setSelectedDays(daysList);
        days.setOnWeekdaysChangeListener(it);
        days.setVisibility(repeat.getCheckedRadioButtonId() == R.id.repeat_days ? View.VISIBLE : View.GONE);

        if(mExpandedItem != null && mExpandedItem.mAlarm == a) {
            it.expand();
        } else {
            it.collapse();
        }
        it.loadRingtoneName();
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
    }

    private class AlarmItem implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, OnWeekdaysChangeListener, TimePickerDialog.OnTimeSetListener {
        private int mPos;
        private Alarm mAlarm;
        private View mRoot;
        AlarmItem(int pos, Alarm a, View root) {
            mPos = pos;
            mAlarm = a;
            mRoot = root;
        }

        public void collapse() {
            mExpandedItem = null;
            mRoot.findViewById(R.id.layout_options).setVisibility(View.GONE);
            mRoot.findViewById(R.id.divider).setVisibility(View.GONE);
            mRoot.findViewById(R.id.btn_delete).setVisibility(View.GONE);

            ImageButton exp = mRoot.findViewById(R.id.expand_btn);
            exp.setImageResource(android.R.drawable.arrow_down_float);
        }

        private void expand() {
            if(mExpandedItem == this)
                return;
            else if(mExpandedItem != null)
                mExpandedItem.collapse();
            mExpandedItem = this;

            mRoot.findViewById(R.id.layout_options).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.divider).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.btn_delete).setVisibility(View.VISIBLE);

            ImageButton exp = mRoot.findViewById(R.id.expand_btn);
            exp.setImageResource(android.R.drawable.arrow_up_float);
        }

        private void toggle() {
            if(mExpandedItem == this) {
                collapse();
            } else {
                expand();
            }
        }

        public void onClick(View view) {
            if(view == mRoot)
                toggle();

            switch(view.getId()) {
                case R.id.expand_btn:
                    toggle();
                    break;
                case R.id.time:
                    TimePickerDialog dialog = new TimePickerDialog(
                            mRoot.getContext(), this, mAlarm.hour, mAlarm.minute,
                            DateFormat.is24HourFormat(mRoot.getContext()));
                    dialog.show();
                    break;
                case R.id.btn_sound:
                    mChangeListener.pickRingtone(mAlarm, mPos);
                    break;
                case R.id.btn_sound_file:
                    mChangeListener.pickSoundFile(mAlarm, mPos);
                    break;
                case R.id.btn_delete:
                    mAlarms.remove(mPos);
                    notifyDataSetChanged();
                    mChangeListener.onAlarmChanged(mAlarm);
                    break;
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton btn, boolean checked) {
            switch(btn.getId()) {
                case R.id.box_vibrate:
                    mAlarm.vibrate = checked;
                    break;
                case R.id.enabled:
                    mAlarm.enabled = checked;
                    break;
                default:
                    return;
            }
            mChangeListener.onAlarmChanged(mAlarm);
        }

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            if(checkedId == R.id.repeat_hour)
                mAlarm.hour = Alarm.HOUR_ANY;
            else if(mAlarm.hour == Alarm.HOUR_ANY)
                mAlarm.hour = Calendar.getInstance().get(Calendar.HOUR);

            for(int i = 0; i < mAlarm.repeatDays.length; ++i) {
                mAlarm.repeatDays[i] = checkedId != R.id.repeat_days;
            }

            TextView time = mRoot.findViewById(R.id.time);
            time.setText(mAlarm.getTimeText());

            WeekdaysPicker days = mRoot.findViewById(R.id.days);
            days.setSelectedDays(mAlarm.getSelectedDays());
            days.setVisibility(checkedId == R.id.repeat_days ? View.VISIBLE : View.GONE);

            mChangeListener.onAlarmChanged(mAlarm);
        }

        @Override
        public void onChange(View view, int clickedDay, List<Integer> selectedDays) {
            mAlarm.repeatDays[clickedDay-1] = selectedDays.contains(clickedDay);
            mChangeListener.onAlarmChanged(mAlarm);
        }

        private void loadRingtoneName() {
            Button sound = mRoot.findViewById(R.id.btn_sound);
            Uri uri = Uri.parse(mAlarm.soundUri);

            Ringtone tone = RingtoneManager.getRingtone(mRoot.getContext(), uri);
            if(tone != null && !uri.getAuthority().equals("com.android.providers.media.documents")) {
                sound.setText(tone.getTitle(mRoot.getContext()));
                return;
            }

            Cursor c = mRoot.getContext().getContentResolver().query(uri,
                    null, null, null, null);
            if(c != null) {
                try {
                    if(c.moveToFirst()) {
                        sound.setText(c.getString(
                                c.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
                        return;
                    }
                } finally {
                    c.close();
                }
            }

            sound.setText(uri.getLastPathSegment());
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            if(mAlarm.hour != Alarm.HOUR_ANY)
                mAlarm.hour = hour;
            mAlarm.minute = minute;
            mChangeListener.onAlarmChanged(mAlarm);

            TextView time = mRoot.findViewById(R.id.time);
            time.setText(mAlarm.getTimeText());
        }
    }
}
